package com.kltn.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageDto {
    private String id;

    private String fileName;

    private String fileType;

    private String Uri;

    private Long idProduct;

}
