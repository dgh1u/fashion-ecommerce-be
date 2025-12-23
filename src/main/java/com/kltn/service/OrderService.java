package com.kltn.service;

import com.kltn.dto.entity.OrderDto;
import com.kltn.dto.request.GetOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderDto> getAllOrders(GetOrderRequest request, Pageable pageable);

    Page<OrderDto> getOrdersByUserId(GetOrderRequest request, Pageable pageable, Long userId);

    OrderDto getOrderById(Long id);

    OrderDto getOrderByIdAndUserId(Long id, Long userId);

    void cancelOrder(Long id);

    void cancelOrderByUserId(Long id, Long userId);

    void updateOrderStatus(Long id, String status);
}