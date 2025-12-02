package com.kltn.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productTitle;
    private Long sizeId;
    private String sizeName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
}