package com.kltn.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
    private String id;

    private String fileName;

    private String fileType;

    private String Uri;

    private Long idProduct;

    private Integer orderIndex;

    // Constructor không có orderIndex để tương thích ngược
    public ImageDto(String id, String fileName, String fileType, String uri, Long idProduct) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.Uri = uri;
        this.idProduct = idProduct;
        this.orderIndex = 0;
    }
}
