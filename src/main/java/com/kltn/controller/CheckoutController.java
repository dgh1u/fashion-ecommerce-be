package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.request.checkout.CheckoutRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.exception.DataNotFoundException;
import com.kltn.service.CheckoutService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final JwtConfig jwtConfig;

    @ApiOperation(value = "Thanh toán giỏ hàng")
    @PostMapping("")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            return BaseResponse.successData(checkoutService.checkout(userId, request));
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }
}