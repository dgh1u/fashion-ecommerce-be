// src/main/java/com/nckh/firstClassroom/dto/entity/DocumentDto.java
package com.kltn.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String fileName;
    private String fileType;
    private String uri;
    private Long idProduct;
}