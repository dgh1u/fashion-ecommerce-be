package com.kltn.mapper;

import com.kltn.dto.entity.ActionDto;
import com.kltn.model.Action;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActionMapper {

    // Giả sử User entity có field là email,
    // và bạn muốn hiển thị email vào ActionDto.username
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")

    // Tương tự, Product có field title -> map sang productTitle
    @Mapping(target = "productTitle", source = "product.title")

    @Mapping(target = "isRead", source = "isRead")
    // Lấy luôn productId
    @Mapping(target = "productId", source = "product.id")

    // Các field còn lại (id, action, time) MapStruct tự map theo tên trùng khớp
    ActionDto toActionDto(Action action);
}
