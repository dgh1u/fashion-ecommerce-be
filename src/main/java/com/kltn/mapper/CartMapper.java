package com.kltn.mapper;

import com.kltn.dto.response.cart.CartItemResponse;
import com.kltn.dto.response.cart.CartResponse;
import com.kltn.model.Cart;
import com.kltn.model.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        Integer totalItems = cart.getCartItems() != null ? cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum() : 0;

        Integer totalAmount = cart.getCartItems() != null ? cart.getCartItems().stream()
                .mapToInt(item -> item.getQuantity() * item.getProduct().getCriteria().getPrice())
                .sum() : 0;

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .items(cart.getCartItems() != null ? cart.getCartItems().stream()
                        .map(this::toCartItemResponse)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        Integer unitPrice = cartItem.getProduct().getCriteria().getPrice();
        Integer totalPrice = cartItem.getQuantity() * unitPrice;

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productTitle(cartItem.getProduct().getTitle())
                .sizeId(cartItem.getSize().getId())
                .sizeName(cartItem.getSize().getName())
                .quantity(cartItem.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}