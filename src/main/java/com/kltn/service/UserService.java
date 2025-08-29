package com.kltn.service;

import com.kltn.dto.entity.UserDto;
import com.kltn.dto.request.CreateUserRequest;
import com.kltn.dto.request.UpdateUserRequest;
import com.kltn.model.User;
import com.kltn.repository.custom.CustomUserQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserService {
    Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest);
    UserDto selectUserByEmail(String email);
    UserDto selectUserById(Long id);
    void changeAvatar(String email, byte[] fileBytes);

    UserDto createUser(CreateUserRequest request);
    UserDto updateUser(UpdateUserRequest request);

    void deleteUser(Long id);
    List<UserDto> deleteAllIdUsers(List<Long> ids);
}
