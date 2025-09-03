package com.kltn.mapper;

import com.kltn.dto.entity.ProductInventoryDto;
import com.kltn.model.ProductInventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SizeMapper.class})
public interface ProductInventoryMapper {
    ProductInventoryDto toProductInventoryDto(ProductInventory productInventory);
    ProductInventory toProductInventory(ProductInventoryDto productInventoryDto);
}
