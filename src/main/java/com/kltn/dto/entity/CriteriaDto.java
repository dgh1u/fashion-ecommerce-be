package com.kltn.dto.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CriteriaDto {
    private Long id;


    private BigDecimal originalPrice;


    private String firstClass;

    private BigDecimal price;



    private Boolean gender;
    private SizeDto size; // Dùng size thay vì idSize



    private String secondClass;

    private String color;
    private String material;


}
