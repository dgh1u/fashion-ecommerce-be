package com.kltn.mapper;

import com.kltn.dto.entity.OrderDto;
import com.kltn.dto.entity.OrderItemDto;
import com.kltn.model.OrderItems;
import com.kltn.model.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    OrderDto toOrderDto(Orders order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    @Mapping(target = "productDescription", source = "product.content")
    @Mapping(target = "sizeId", source = "size.id")
    @Mapping(target = "sizeName", source = "size.name")
    OrderItemDto toOrderItemDto(OrderItems orderItem);
}