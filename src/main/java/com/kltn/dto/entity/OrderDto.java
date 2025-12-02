package com.kltn.dto.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDto {
    private Long id;
    private Long orderCode;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private String notes;
    private Integer totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin chi tiết đơn hàng
    private List<OrderItemDto> orderItems;

    // Thông tin người dùng
    private String userFullName;
    private String userEmail;
}