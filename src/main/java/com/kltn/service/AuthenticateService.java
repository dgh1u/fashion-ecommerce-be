package com.kltn.service;

import com.kltn.dto.entity.UserDto;
import com.kltn.dto.request.*;
import com.kltn.dto.request.*;
import com.kltn.dto.response.LoginResponse;
import com.kltn.dto.response.RegisterResponse;

public interface AuthenticateService {
    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
    String verifyAccount(VerifyAccountRequest request);

    String regenerateOTP(RegenerateOtpRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    UserDto updateProfile(UpdateProfileRequest request);
}
