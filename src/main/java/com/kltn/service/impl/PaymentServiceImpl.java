package com.kltn.service.impl;

import com.kltn.dto.request.payment.CreatePaymentRequest;
import com.kltn.dto.request.payment.PaymentReceiveHookRequest;
import com.kltn.exception.DataExistException;
import com.kltn.model.Orders;
import com.kltn.model.PaymentHistory;
import com.kltn.repository.OrderRepository;
import com.kltn.repository.PaymentRepository;
import com.kltn.repository.custom.CustomPaymentQuery;
import com.kltn.service.PaymentService;
import com.kltn.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Override
    public CreatePaymentLinkResponse createPayment(CreatePaymentRequest request, Long id) {
        try {
            log.info("Tạo thanh toán cho userId: {}", id);
            String currentTimeString = String.valueOf(new Date().getTime());
            Long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            // Tạo và lưu PaymentHistory (trạng thái pending) để lấy paymentHistoryId
            PaymentHistory paymentHistory = new PaymentHistory();
            paymentHistory.setOrderCode(orderCode);
            paymentHistory.setAmount(request.getPrice());
            paymentHistory.setDescription(id + " " + request.getDesc());
            paymentHistory.setSuccess(false);
            paymentHistory = paymentRepository.save(paymentHistory);

            // Xây dựng URL chuyển hướng theo mẫu:
            // FRONTEND_URL/payment/{paymentHistoryId}/result
            String resultUrl = frontendUrl + "/" + paymentHistory.getId() + "/result";

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(id + " " + request.getDesc())
                    .quantity(Integer.valueOf(1))
                    .price(Long.valueOf(request.getPrice()))
                    .build();
            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(Long.valueOf(request.getPrice()))
                    .description(id + " " + request.getDesc())
                    .returnUrl(resultUrl)
                    .cancelUrl(resultUrl)
                    .items(java.util.Collections.singletonList(item))
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            // Cập nhật PaymentHistory với paymentLinkId trả về từ PayOS
            paymentHistory.setPaymentLinkId(data.getPaymentLinkId());
            paymentRepository.save(paymentHistory);

            return data;
        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán: {}", e.getMessage(), e);
            throw new DataExistException("Thanh toán thất bại: " + e.getMessage());
        }
    }

    @Override
    public CreatePaymentLinkResponse createPaymentLinkForOrder(CreatePaymentRequest request, Long userId,
            Long orderId) {
        try {
            log.info("Tạo thanh toán cho đơn hàng orderId: {} của user: {}", orderId, userId);

            // Lấy order để có order code
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new DataExistException("Không tìm thấy đơn hàng"));

            Long orderCode = order.getOrderCode();

            // Tạo và lưu PaymentHistory (trạng thái pending) để lấy paymentHistoryId
            PaymentHistory paymentHistory = new PaymentHistory();
            paymentHistory.setOrderCode(orderCode);
            paymentHistory.setAmount(request.getPrice());
            paymentHistory.setDescription(request.getDesc());
            paymentHistory.setSuccess(false);
            paymentHistory.setUserId(userId);
            paymentHistory.setOrder(order);
            paymentHistory = paymentRepository.save(paymentHistory);

            // Xây dựng URL chuyển hướng theo mẫu:
            // FRONTEND_URL/payment/{paymentHistoryId}/result
            String resultUrl = frontendUrl + "/" + paymentHistory.getId() + "/result";

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(request.getDesc())
                    .quantity(Integer.valueOf(1))
                    .price(Long.valueOf(request.getPrice()))
                    .build();
            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(Long.valueOf(request.getPrice()))
                    .description(
                            request.getDesc().length() > 25 ? request.getDesc().substring(0, 25) : request.getDesc())
                    .returnUrl(resultUrl)
                    .cancelUrl(resultUrl)
                    .items(java.util.Collections.singletonList(item))
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            // Cập nhật PaymentHistory với paymentLinkId trả về từ PayOS
            paymentHistory.setPaymentLinkId(data.getPaymentLinkId());
            paymentRepository.save(paymentHistory);

            return data;
        } catch (Exception e) {
            log.error("Lỗi khi tạo thanh toán cho đơn hàng: {}", e.getMessage(), e);
            throw new DataExistException("Thanh toán thất bại: " + e.getMessage());
        }
    }

    @Override
    public void receiveHook(PaymentReceiveHookRequest request) {
        try {
            // Lấy userid từ description hoặc từ PaymentHistory có sẵn
            Long orderCode = request.getData().getOrderCode();
            log.info("=== Xử lý webhook cho orderCode: {} ===", orderCode);
            log.info("Webhook data: success={}, amount={}", request.isSuccess(), request.getData().getAmount());

            // Thử tìm PaymentHistory với fetch Order
            Optional<PaymentHistory> phOpt = paymentRepository.findByOrderCodeWithOrder(orderCode);

            if (!phOpt.isPresent()) {
                log.error("Không tìm thấy PaymentHistory với orderCode: {} qua findByOrderCodeWithOrder", orderCode);
                // Thử tìm lại bằng phương thức thông thường
                phOpt = paymentRepository.findByOrderCode(orderCode);
                if (!phOpt.isPresent()) {
                    log.error("Không tìm thấy PaymentHistory với orderCode: {} qua findByOrderCode", orderCode);

                    // Log tất cả PaymentHistory để debug
                    log.info("Danh sách tất cả PaymentHistory trong DB:");
                    paymentRepository.findAll().forEach(ph -> {
                        log.info("  - ID: {}, OrderCode: {}, Amount: {}", ph.getId(), ph.getOrderCode(),
                                ph.getAmount());
                    });

                    throw new DataExistException("Không tìm thấy giao dịch tạm với orderCode: " + orderCode);
                }
            }

            PaymentHistory payment = phOpt.get();
            log.info("Tìm thấy PaymentHistory: ID={}, OrderCode={}, UserId={}",
                    payment.getId(), payment.getOrderCode(), payment.getUserId());

            // Cập nhật các thông tin từ dữ liệu PayOS
            payment.setCode(request.getCode());
            payment.setDescrip(request.getDesc());
            payment.setSuccess(request.isSuccess());
            payment.setDescStatus(request.getData().getDesc());
            payment.setAmount(request.getData().getAmount() != null ? request.getData().getAmount().intValue() : null);
            payment.setDescription(request.getData().getDescription());
            payment.setAccountNumber(request.getData().getAccountNumber());
            payment.setReference(request.getData().getReference());
            payment.setTransactionDateTime(request.getData().getTransactionDateTime());
            payment.setCurrency(request.getData().getCurrency());
            payment.setPaymentLinkId(request.getData().getPaymentLinkId());
            payment.setCounterAccountBankId(request.getData().getCounterAccountBankId());
            payment.setCounterAccountBankName(request.getData().getCounterAccountBankName());
            payment.setCounterAccountName(request.getData().getCounterAccountName());
            payment.setCounterAccountNumber(request.getData().getCounterAccountNumber());
            payment.setVirtualAccountName(request.getData().getVirtualAccountName());
            payment.setVirtualAccountNumber(request.getData().getVirtualAccountNumber());
            payment.setSignature(request.getSignature());

            paymentRepository.save(payment);
            log.info("Đã cập nhật PaymentHistory ID: {}", payment.getId());

            // Nếu thanh toán thành công và có đơn hàng, cập nhật trạng thái đơn hàng
            if (request.isSuccess() && payment.getOrder() != null) {
                try {
                    Orders order = payment.getOrder();
                    log.info("Cập nhật đơn hàng: ID={}, OrderCode={}, Status hiện tại={}",
                            order.getId(), order.getOrderCode(), order.getStatus());

                    // Đảm bảo order được load đầy đủ
                    if (order.getId() != null) {
                        order.setStatus("paid");
                        orderRepository.save(order);
                        log.info("✅ Đã cập nhật trạng thái đơn hàng {} thành 'paid'", order.getOrderCode());

                        // Cập nhật inventory sau khi thanh toán thành công
                        try {
                            inventoryService.updateInventoryAfterPayment(order);
                            log.info("✅ Đã cập nhật inventory cho đơn hàng {}", order.getOrderCode());
                        } catch (Exception inventoryEx) {
                            log.error("❌ Lỗi khi cập nhật inventory cho đơn hàng {}: {}",
                                    order.getOrderCode(), inventoryEx.getMessage());
                            // Không throw exception để không ảnh hưởng đến webhook
                        }
                    }
                } catch (Exception orderEx) {
                    log.error("❌ Lỗi khi cập nhật đơn hàng: {}", orderEx.getMessage(), orderEx);
                    // Vẫn tiếp tục xử lý webhook thành công
                }
            } else {
                if (!request.isSuccess()) {
                    log.info("Thanh toán không thành công, không cập nhật đơn hàng");
                }
                if (payment.getOrder() == null) {
                    log.warn("PaymentHistory không có liên kết với Order");
                }
            }

            log.info("=== Hoàn thành xử lý webhook cho orderCode: {} ===", orderCode);

        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý webhook: {}", e.getMessage(), e);
            throw new DataExistException("Thanh toán thất bại: " + e.getMessage());
        }
    }

    @Override
    public Page<PaymentHistory> getAllPayment(CustomPaymentQuery.PaymentFilterParam param, PageRequest pageRequest) {
        Specification<PaymentHistory> specification = CustomPaymentQuery.getFilterPayment(param);
        return paymentRepository.findAll(specification, pageRequest);
    }
}
