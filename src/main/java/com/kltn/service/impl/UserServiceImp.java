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

    /**
     * Lấy tất cả người dùng với phân trang và filter
     * Sử dụng Specification để lọc theo tiêu chí
     */
    @Override
    public Page<User> getAllUser(CustomUserQuery.UserFilterParam param, PageRequest pageRequest) {
        Specification<User> specification = CustomUserQuery.getFilterUser(param);
        return userRepository.findAll(specification, pageRequest);
    }

    /**
     * Tìm người dùng theo email
     * Kiểm tra sự tồn tại và trả về UserDto
     */
    @Override
    public UserDto selectUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Email không tồn tại");
        }
        User user = userOptional.get();
        return userMapper.toUserDto(user);
    }

    /**
     * Tìm người dùng theo ID
     * Kiểm tra sự tồn tại và trả về UserDto
     */
    @Override
    public UserDto selectUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        User user = userOptional.get();
        return userMapper.toUserDto(user);
    }

    /**
     * Thay đổi avatar của người dùng
     * Mã hóa ảnh thành Base64 và lưu vào database
     */
    @Override
    public void changeAvatar(String email, byte[] fileBytes) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Email không tồn tại");
        }
        User user = userOptional.get();
        user.setB64(Base64.getEncoder().encodeToString(fileBytes));
        userRepository.saveAndFlush(user);
    }

    /**
     * Tạo người dùng mới
     * Kiểm tra email tồn tại, mã hóa mật khẩu và gán role
     */
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
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm người dùng");
        }
    }

    /**
     * Cập nhật thông tin người dùng
     * Giữ nguyên mật khẩu, cập nhật thông tin khác và role
     */
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
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhât người dùng");
        }
    }

    /**
     * Xóa người dùng theo ID
     * Kiểm tra tồn tại trước khi xóa
     */
    @Override
    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình xóa người dùng");
        }
    }

    /**
     * Xóa nhiều người dùng theo danh sách ID
     * Trả về danh sách người dùng đã bị xóa
     */
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

    /**
     * Tìm và trả về Role theo roleId
     * Sử dụng trong quá trình tạo và cập nhật user
     */
    private Role buildRole(String roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new MyCustomException("Role không tồn tại!"));
    }
}
