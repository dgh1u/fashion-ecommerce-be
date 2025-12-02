package com.kltn.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productTitle;
    private Long sizeId;
    private String sizeName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
}