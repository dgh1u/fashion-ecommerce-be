package com.kltn.service.impl;

import com.kltn.dto.request.checkout.CheckoutRequest;
import com.kltn.dto.request.payment.CreatePaymentRequest;
import com.kltn.dto.response.payment.CreatePaymentResponse;
import com.kltn.exception.DataNotFoundException;
import com.kltn.model.*;
import com.kltn.repository.*;
import com.kltn.service.CheckoutService;
import com.kltn.service.CartService;
import com.kltn.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final UserRepository userRepository;

    @Override
    public CreatePaymentResponse checkout(Long userId, CheckoutRequest request) {
        try {
            // 1. Lấy giỏ hàng
            Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
            if (!cartOpt.isPresent() || cartOpt.get().getCartItems().isEmpty()) {
                throw new DataNotFoundException("Giỏ hàng trống");
            }

            Cart cart = cartOpt.get();

            // 2. Tính tổng tiền và kiểm tra
            int calculatedTotal = cart.getCartItems().stream()
                    .mapToInt(item -> item.getQuantity() * item.getProduct().getCriteria().getPrice())
                    .sum();

            if (!Objects.equals(calculatedTotal, request.getTotalAmount())) {
                throw new DataNotFoundException("Tổng tiền không khớp");
            }

            // 3. Tạo đơn hàng
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

            // Tạo orderCode unique
            String currentTimeString = String.valueOf(new Date().getTime());
            Long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            Orders order = new Orders();
            order.setOrderCode(orderCode);
            order.setUser(user);
            order.setTotalAmount(request.getTotalAmount());
            order.setStatus("pending");
            order.setCustomerName(request.getCustomerName());
            order.setCustomerPhone(request.getCustomerPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setNotes(request.getNotes());

            order = orderRepository.save(order);

            // 4. Tạo order items
            for (CartItem cartItem : cart.getCartItems()) {
                OrderItems orderItem = new OrderItems();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSize(cartItem.getSize());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(cartItem.getProduct().getCriteria().getPrice());
                orderItem.setTotalPrice(cartItem.getQuantity() * cartItem.getProduct().getCriteria().getPrice());

                orderItemRepository.save(orderItem);
            }

            // 5. Tạo payment request
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setPrice(request.getTotalAmount());
            // PayOS yêu cầu description tối đa 25 ký tự
            String description = "DH#" + orderCode;
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }
            paymentRequest.setDesc(description);

            // 6. Tạo payment link với PayOS
            CreatePaymentLinkResponse paymentData = paymentService.createPaymentLinkForOrder(paymentRequest, userId,
                    order.getId());

            // 7. Xóa giỏ hàng sau khi tạo đơn hàng thành công
            cartService.clearCart(userId);

            log.info("Đã tạo đơn hàng {} và payment link cho user {}", orderCode, userId);

            return CreatePaymentResponse.builder()
                    .url(paymentData.getCheckoutUrl())
                    .orderCode(orderCode)
                    .orderId(order.getId())
                    .build();

        } catch (Exception e) {
            log.error("Lỗi checkout cho user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi thanh toán: " + e.getMessage());
        }
    }
}