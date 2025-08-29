package com.kltn.dto.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CriteriaDto {
    private Long id;
    private Double acreage;
    private String address;
    private Boolean airConditioner;
    private Boolean interior;
    private BigDecimal originalPrice;
    private Boolean heater;
    private Boolean internet;
    private String firstClass;
    private Boolean parking;
    private BigDecimal price;
    private Boolean owner;
    private Boolean toilet;
    private Boolean time;
    private BigDecimal waterPrice;

    private Boolean gender;
    private SizeDto size; // Dùng size thay vì idSize
    private Boolean kitchen;
    private Boolean security;

    private String openHours;
    private String secondClass;
    private Boolean delivery;
    private Boolean dineIn;
    private Boolean takeAway;
    private Boolean bigSpace;
    private String linkShopeeFood;
    private String major;
    private String referenceUrl;

}
