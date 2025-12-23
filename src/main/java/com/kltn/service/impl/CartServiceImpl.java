package com.kltn.service.impl;

import com.kltn.dto.request.cart.AddToCartRequest;
import com.kltn.dto.request.cart.UpdateCartItemRequest;
import com.kltn.dto.response.cart.CartResponse;
import com.kltn.exception.DataNotFoundException;
import com.kltn.mapper.CartMapper;
import com.kltn.model.*;
import com.kltn.repository.*;
import com.kltn.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    /**
     * Lấy giỏ hàng của người dùng theo userId
     * Nếu chưa có giỏ hàng thì tạo mới
     */
    @Override
    public CartResponse getCartByUserId(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        if (cartOpt.isPresent()) {
            return cartMapper.toCartResponse(cartOpt.get());
        }

        // Nếu chưa có cart thì tạo mới
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart = cartRepository.save(cart);

        return cartMapper.toCartResponse(cart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã tồn tại trong giỏ hàng thì tăng số lượng
     * Nếu chưa tồn tại thì tạo mới cart item
     */
    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // Kiểm tra product và size có tồn tại không
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm"));

        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy size"));

        // Tìm hoặc tạo cart cho user
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Kiểm tra xem sản phẩm với size này đã có trong cart chưa
        Optional<CartItem> existingItemOpt = cartItemRepository
                .findByCartIdAndProductIdAndSizeId(cart.getId(), request.getProductId(), request.getSizeId());

        if (existingItemOpt.isPresent()) {
            // Nếu đã có thì tăng số lượng
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // Nếu chưa có thì tạo mới
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setSize(size);
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        return getCartByUserId(userId);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Kiểm tra quyền sở hữu trước khi cập nhật
     */
    @Override
    public CartResponse updateCartItem(Long userId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new DataNotFoundException("Bạn không có quyền thao tác với sản phẩm này");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCartByUserId(userId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * Kiểm tra quyền sở hữu trước khi xóa
     */
    @Override
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));

        // Kiểm tra quyền sở hữu
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new DataNotFoundException("Bạn không có quyền thao tác với sản phẩm này");
        }

        cartItemRepository.delete(cartItem);
        return getCartByUserId(userId);
    }

    /**
     * Xóa toàn bộ sản phẩm trong giỏ hàng của người dùng
     */
    @Override
    public void clearCart(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        if (cartOpt.isPresent()) {
            cartItemRepository.deleteByCartId(cartOpt.get().getId());
        }
    }
}