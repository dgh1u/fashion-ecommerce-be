package com.kltn.dto.response.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateProductResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime lastUpdate;
    private String user;
    private Long criteriaId;
    private String type;
}
