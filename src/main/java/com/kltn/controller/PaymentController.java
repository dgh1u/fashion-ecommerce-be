package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.request.payment.GetPaymentRequest;
import com.kltn.dto.request.payment.CreatePaymentRequest;
import com.kltn.dto.request.payment.PaymentReceiveHookRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.dto.response.OrderPaymentResultDTO;
import com.kltn.dto.response.OrderItemDTO;
import com.kltn.dto.response.payment.CreatePaymentResponse;
import com.kltn.exception.DataExistException;
import com.kltn.mapper.PaymentMapper;
import com.kltn.model.PaymentHistory;
import com.kltn.model.OrderItems;
import com.kltn.model.Orders;
import com.kltn.repository.PaymentRepository;
import com.kltn.repository.OrderItemRepository;
import com.kltn.service.PaymentService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtConfig jwtConfig;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String token) throws Exception {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        System.out.println("cliams: " + claims);
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());

            CreatePaymentLinkResponse data = paymentService.createPayment(request, userId);
            String checkoutUrl = data.getCheckoutUrl();
            return ResponseEntity.ok(CreatePaymentResponse.builder().url(checkoutUrl).build());
        }
        throw new DataExistException("Thanh toán thất bại");
    }

    // https://464d-2402-800-619d-1df1-f009-e0b5-cdc7-1b12.ngrok-free.app/api/payment/receive-hook
    @PostMapping("/receive-hook")
    public ResponseEntity<?> receiveHook(@RequestBody PaymentReceiveHookRequest request) throws Exception {
        paymentService.receiveHook(request);
        return ResponseEntity.ok("Chuyển khoản paypos");
    }

    @ApiOperation(value = "Lấy lịch sử giao dịch")
    @GetMapping("")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllPayment(@Valid @ModelAttribute GetPaymentRequest request) {
        Page<PaymentHistory> page = paymentService.getAllPayment(request,
                PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(paymentMapper::toPaymentDTO)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/result/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPaymentResult(@PathVariable Long id) {
        // Sử dụng method với fetch để tránh lazy loading issue nếu cần thiết
        Optional<PaymentHistory> paymentOpt = paymentRepository.findById(id);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body("Giao dịch không tồn tại");
        }
        PaymentHistory paymentHistory = paymentOpt.get();
        return ResponseEntity.ok(BaseResponse.successData(paymentMapper.toPaymentDTO(paymentHistory)));
    }

    @GetMapping("/result/{id}/order")
    @ApiOperation(value = "Lấy thông tin đơn hàng từ payment result")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderFromPaymentResult(@PathVariable Long id) {
        // Tìm PaymentHistory với Order
        Optional<PaymentHistory> paymentOpt = paymentRepository.findByIdWithOrder(id);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body("Giao dịch không tồn tại");
        }

        PaymentHistory paymentHistory = paymentOpt.get();
        if (paymentHistory.getOrder() == null) {
            return ResponseEntity.status(404).body("Không tìm thấy đơn hàng liên quan");
        }

        Orders order = paymentHistory.getOrder();

        // Lấy danh sách OrderItems
        List<OrderItems> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(item -> {
                    return OrderItemDTO.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productTitle(item.getProduct().getTitle())
                            .sizeId(item.getSize().getId())
                            .sizeName(item.getSize().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // Tạo response với thông tin Order + Payment status + OrderItems
        OrderPaymentResultDTO result = OrderPaymentResultDTO.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .notes(order.getNotes())
                .orderItems(orderItemDTOs)
                .paymentSuccess(paymentHistory.isSuccess())
                .paymentAmount(paymentHistory.getAmount())
                .transactionDateTime(paymentHistory.getTransactionDateTime())
                .reference(paymentHistory.getReference())
                .build();

        return ResponseEntity.ok(BaseResponse.successData(result));
    }

    @GetMapping("/order/{orderCode}/status")
    @ApiOperation(value = "Kiểm tra trạng thái thanh toán đơn hàng")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable Long orderCode) {
        Optional<PaymentHistory> paymentOpt = paymentRepository.findByOrderCode(orderCode);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body("Không tìm thấy giao dịch");
        }
        PaymentHistory paymentHistory = paymentOpt.get();

        return ResponseEntity.ok(BaseResponse.successData(paymentMapper.toPaymentDTO(paymentHistory)));
    }

}
