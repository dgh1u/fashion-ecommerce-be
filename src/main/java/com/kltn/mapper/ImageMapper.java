package com.kltn.mapper;

import com.kltn.dto.entity.ImageDto;
import com.kltn.model.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "idProduct", source = "product.id")
    ImageDto toDto(Image image);

    Image toImage(ImageDto imageDto);
}
