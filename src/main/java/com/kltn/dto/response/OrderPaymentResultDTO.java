package com.kltn.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderPaymentResultDTO {
    private Long orderId;
    private Long orderCode;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private Integer totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String notes;

    // Order items
    private List<OrderItemDTO> orderItems;

    // Payment info
    private boolean paymentSuccess;
    private Integer paymentAmount;
    private String transactionDateTime;
    private String reference;
}