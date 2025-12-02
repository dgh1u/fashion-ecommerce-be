package com.kltn.dto.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productDescription;
    private Long sizeId;
    private String sizeName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
}