package com.kltn.mapper;

import com.kltn.dto.entity.SizeDto;
import com.kltn.model.Size;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SizeMapper {

    // Ánh xạ từ idSize thành Size
    Size toSize(Long idSize);

    SizeDto toSizeDto(Size size);
}
