// src/main/java/com/nckh/firstClassroom/mapper/DocumentMapper.java
package com.kltn.mapper;

import com.kltn.dto.entity.DocumentDto;
import com.kltn.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "idProduct", source = "product.id")
    DocumentDto toDto(Document document);
}