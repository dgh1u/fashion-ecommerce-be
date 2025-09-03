package com.kltn.dto.request.criteria;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCriteriaRequest {
    // Giá điện (phải lớn hơn 0)
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá điện phải lớn hơn 0")
    private BigDecimal originalPrice;

    // Giá trọ (phải lớn hơn 0)
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    // Loại hình nhà trọ (bắt buộc)
    @NotNull(message = "Thông tin về loại hình là bắt buộc")
    private String firstClass;

    // Giới tính ưu tiên (true: Nam, false: Nữ, null: Không yêu cầu)
    private Boolean gender;

    // Mã Quận (không được để trống)
    @NotNull(message = "Mã quận là bắt buộc")
    private Long idSize;

    // Loại cơ sở lưu trú thứ hai
    private String secondClass;

    private String color;

    // Link tham khảo
    private String material;

}
