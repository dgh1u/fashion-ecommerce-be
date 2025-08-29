package com.kltn.mapper;

import com.kltn.dto.entity.UserDto;
import com.kltn.dto.request.CreateUserRequest;
import com.kltn.dto.request.UpdateUserRequest;
import com.kltn.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "role",ignore = true)
    User toUpdateUser(UpdateUserRequest userRequest);

    @Mapping(target = "password",ignore = true)
    @Mapping(target = "role",ignore = true)
    User toCreateUser(CreateUserRequest userRequest);

    UserDto toUserDto(User user);
}
