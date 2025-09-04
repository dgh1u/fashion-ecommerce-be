package com.kltn.dto.request.product;

import com.kltn.dto.entity.CriteriaDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateProductRequest {
    @NotNull
    @Size(min = 10, max = 100)
    private String title;
    @NotNull
    @Size(min = 50, max = 500)
    private String content;
    private CriteriaDto criteria;

    // Thêm inventory để xử lý size và số lượng
    private Map<Long, Integer> inventory;
}
