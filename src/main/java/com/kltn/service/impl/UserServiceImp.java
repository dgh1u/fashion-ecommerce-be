package com.kltn.service.impl;

import com.kltn.dto.entity.UserDto;
import com.kltn.dto.request.CreateUserRequest;
import com.kltn.dto.request.UpdateUserRequest;
import com.kltn.exception.DataExistException;
import com.kltn.exception.MyCustomException;
import com.kltn.mapper.UserMapper;
import com.kltn.model.Role;
import com.kltn.model.User;
import com.kltn.repository.RoleRepository;
import com.kltn.repository.UserRepository;
import com.kltn.repository.custom.CustomUserQuery;
import com.kltn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest) {
        Specification<User> specification = CustomUserQuery.getFilterUser(param);
        return userRepository.findAll(specification, pageRequest);
    }

    @Override
    public UserDto selectUserByEmail(String email) {
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(!userOptional.isPresent()){
            throw new DataExistException("Email không tồn tại");
        }
        User user=userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto selectUserById(Long id) {
        Optional<User> userOptional=userRepository.findById(id);
        if(!userOptional.isPresent()){
            throw new DataExistException("Người dùng không tồn tại");
        }
        User user=userOptional.get();
        return userMapper.toUserDto(user);
    }

    @Override
    public void changeAvatar(String email, byte[] fileBytes) {
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(!userOptional.isPresent()){
            throw new DataExistException("Email không tồn tại");
        }
        User user=userOptional.get();
        user.setB64(Base64.getEncoder().encodeToString(fileBytes));
        userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            throw new DataExistException("Email đã tồn tại");
        }
        try {
            User user = userMapper.toCreateUser(request);
            user.setRole(buildRole(request.getRoleId()));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setBlock(false);

            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm người dùng");
        }
    }

    @Override
    @Transactional
    public UserDto updateUser(UpdateUserRequest request) {
        Optional<User> userOptional = userRepository.findById(request.getId());
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }

        try {
            User user = userMapper.toUpdateUser(request);
            user.setRole(buildRole(request.getRoleId()));
            user.setPassword(userOptional.get().getPassword());
            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhât người dùng");
        }
    }

    @Override
    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        try {
            userRepository.deleteById(id);
        }catch (Exception e){
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa người dùng");
        }
    }

    @Override
    public List<UserDto> deleteAllIdUsers(List<Long> ids) {
        List<UserDto> userDtos = new ArrayList<>();
        for (Long id : ids) {
            Optional<User> optionalNews = userRepository.findById(id);
            if (optionalNews.isPresent()) {
                User user = optionalNews.get();
                userDtos.add(userMapper.toUserDto(user));
                userRepository.delete(user);
            } else {
                throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa danh sách người dùng!");
            }
        }
        return userDtos;
    }

    private Role buildRole(String roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new MyCustomException("Role không tồn tại!"));
    }
}
