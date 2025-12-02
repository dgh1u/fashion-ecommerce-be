package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.request.cart.AddToCartRequest;
import com.kltn.dto.request.cart.UpdateCartItemRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.exception.DataNotFoundException;
import com.kltn.service.CartService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtConfig jwtConfig;

    @ApiOperation(value = "Lấy giỏ hàng của người dùng")
    @GetMapping("")
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            return BaseResponse.successData(cartService.getCartByUserId(userId));
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }

    @ApiOperation(value = "Thêm sản phẩm vào giỏ hàng")
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            return BaseResponse.successData(cartService.addToCart(userId, request));
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }

    @ApiOperation(value = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            return BaseResponse.successData(cartService.updateCartItem(userId, request));
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }

    @ApiOperation(value = "Xóa sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId,
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            return BaseResponse.successData(cartService.removeFromCart(userId, cartItemId));
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }

    @ApiOperation(value = "Xóa toàn bộ giỏ hàng")
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        Object userIdObj = claims.get("userId");

        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());
            cartService.clearCart(userId);
            return BaseResponse.successData("Đã xóa toàn bộ giỏ hàng");
        }
        throw new DataNotFoundException("Không tìm thấy thông tin người dùng");
    }
}