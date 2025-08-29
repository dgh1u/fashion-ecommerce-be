package com.kltn.dto.entity;

import lombok.Data;

@Data
public class SearchDto {
    private double acreageStart;

    private double acreageEnd;

    private double priceStart;

    private double priceEnd;

    private int firstClass;

    private long idSize;

    private double radius;
}
