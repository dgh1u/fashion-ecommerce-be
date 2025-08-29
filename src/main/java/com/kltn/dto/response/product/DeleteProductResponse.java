package com.kltn.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteProductResponse {
    private Long productId;
    private String message;
    private boolean deleted;
}