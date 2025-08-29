package com.kltn.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiddenProductResponse {
    private Long productId; // ID của bài đăng
    private String message; // Nội dung thông báo
    private boolean hidden;
}
