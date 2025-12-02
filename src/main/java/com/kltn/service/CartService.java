package com.kltn.service;

import com.kltn.dto.request.cart.AddToCartRequest;
import com.kltn.dto.request.cart.UpdateCartItemRequest;
import com.kltn.dto.response.cart.CartResponse;

public interface CartService {
    CartResponse getCartByUserId(Long userId);

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateCartItem(Long userId, UpdateCartItemRequest request);

    CartResponse removeFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);
}