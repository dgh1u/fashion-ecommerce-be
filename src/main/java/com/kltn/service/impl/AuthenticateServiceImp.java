package com.kltn.service.impl;

import com.kltn.config.JwtConfig;
import com.kltn.constant.RoleEnum;
import com.kltn.dto.custom.CustomUserDetails;
import com.kltn.dto.entity.UserDto;
import com.kltn.dto.request.*;
import com.kltn.dto.request.*;
import com.kltn.dto.response.LoginResponse;
import com.kltn.dto.response.RegisterResponse;
import com.kltn.exception.AuthenticateException;
import com.kltn.exception.DataExistException;
import com.kltn.exception.DataNotFoundException;
import com.kltn.exception.MyCustomException;
import com.kltn.mapper.UserMapper;
import com.kltn.model.Role;
import com.kltn.model.User;
import com.kltn.repository.RoleRepository;
import com.kltn.repository.UserRepository;
import com.kltn.service.AuthenticateService;
import com.kltn.utils.MailUtil;
import com.kltn.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticateServiceImp implements AuthenticateService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtConfig jwtConfig;

    private final AuthenticationManager authenticationManager;

    private final MailUtil mailUtil;

    private final UserMapper userMapper;

    /**
     * Xử lý đăng nhập người dùng
     * Nhiệm vụ:
     * - Kiểm tra email có tồn tại trong hệ thống
     * - Xác thực mật khẩu
     * - Kiểm tra trạng thái khóa tài khoản
     * - Tạo JWT token
     * - Trả về thông tin người dùng và token
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            throw new AuthenticateException("Email không tồn tại");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticateException("Mật khẩu không chính xác");
        }
        if (user.getBlock()) {
            throw new AuthenticateException("Tài khoản bị khóa");
        }

        // Tạo Authentication object
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Tạo JWT token và trả về response
        return LoginResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .token(jwtConfig.generateToken(user))
                .roles(roles)
                .build();
    }

    /**
     * Xử lý đăng ký tài khoản mới
     * Nhiệm vụ:
     * - Kiểm tra email đã tồn tại chưa
     * - Tạo tài khoản người dùng mới với role CUSTOMER
     * - Mã hóa mật khẩu
     * - Lưu thông tin người dùng vào database
     * - Trả về thông tin người dùng đã đăng ký
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            throw new DataExistException("Email đã tồn tại");
        }
        User user = new User();

        Optional<Role> customerRole = roleRepository.findById(RoleEnum.CUSTOMER.name());
        Role role = customerRole.get();
        if (!customerRole.isPresent()) {
            role.setRoleId(RoleEnum.CUSTOMER.name());
            role.setName("Khách hàng");
            role.setDescription("Khách hàng");
            role = roleRepository.saveAndFlush(role);
        }
        user.setAddress(request.getAddress());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setIsSuperAdmin(false);
        user.setBlock(false);
        user.setPhone(request.getPhone());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(role);

        userRepository.saveAndFlush(user);

        return RegisterResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .phone(user.getPhone())
                .build();
    }

    /**
     * Xử lý xác thực tài khoản qua OTP
     * Nhiệm vụ:
     * - Kiểm tra email có tồn tại
     * - Xác thực OTP và thời gian hiệu lực (2 phút)
     * - Mở khóa tài khoản nếu OTP hợp lệ
     * - Trả về kết quả xác thực
     */
    @Override
    public String verifyAccount(VerifyAccountRequest request) {
        String email = request.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataNotFoundException("Không tồn tại người dùng có email là: " + email);
        }
        User user = userOptional.get();
        if (user.getOtp().equals(request.getOtp()) &&
                Duration.between(user.getOtpGeneratedTime(), Instant.now()).getSeconds() < (2 * 60)) {
            user.setBlock(false);
            userRepository.save(user);
            return "OTP đã xác thực thành công.";
        } else {
            throw new MyCustomException("Hãy tạo lại OTP và thử lại.");
        }
    }

    /**
     * Xử lý tạo lại mã OTP mới
     * Nhiệm vụ:
     * - Kiểm tra email có tồn tại
     * - Tạo mã OTP mới
     * - Cập nhật thời gian tạo OTP
     * - Gửi email chứa OTP đến người dùng
     * - Trả về thông báo thành công
     */
    @Override
    public String regenerateOTP(RegenerateOtpRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            throw new DataNotFoundException("Không tồn tại người dùng có email là: " + request.getEmail());
        }
        String otp = OtpUtil.generateOtp();
        User user = userOptional.get();
        user.setOtp(otp);
        user.setOtpGeneratedTime(Instant.now());
        userRepository.save(user);
        mailUtil.sendOtpEmail(request.getEmail(), otp);
        return "Email đã gửi... Hãy xác thực trong 2 phút";
    }

    /**
     * Xử lý quên mật khẩu và đặt lại mật khẩu mới
     * Nhiệm vụ:
     * - Kiểm tra email có tồn tại
     * - Xác thực OTP và thời gian hiệu lực (1 phút)
     * - Mã hóa và cập nhật mật khẩu mới
     * - Lưu thông tin vào database
     * - Trả về kết quả xác thực
     */
    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            throw new DataNotFoundException("Không tồn tại người dùng có email là: " + email);
        }
        User user = userOptional.get();
        if (user.getOtp().equals(request.getOtp()) &&
                Duration.between(user.getOtpGeneratedTime(), Instant.now()).getSeconds() < (1 * 60)) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);
            return "OTP đã xác thực thành công.";
        } else {
            throw new MyCustomException("Hãy tạo lại OTP và thử lại.");
        }
    }

    /**
     * Xử lý cập nhật thông tin cá nhân người dùng
     * Nhiệm vụ:
     * - Kiểm tra người dùng có tồn tại
     * - Cập nhật họ tên, số điện thoại, địa chỉ
     * - Lưu thông tin vào database
     * - Trả về thông tin người dùng đã cập nhật
     */
    @Override
    public UserDto updateProfile(UpdateProfileRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (!userOptional.isPresent()) {
            throw new DataExistException("Người dùng không tồn tại");
        }
        try {
            User user = userOptional.get();
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            return userMapper.toUserDto(userRepository.saveAndFlush(user));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhât người dùng");
        }
    }
}
