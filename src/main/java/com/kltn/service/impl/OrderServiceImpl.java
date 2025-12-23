package com.kltn.service.impl;

import com.kltn.dto.entity.OrderDto;
import com.kltn.dto.entity.OrderItemDto;
import com.kltn.dto.request.GetOrderRequest;
import com.kltn.mapper.OrderMapper;
import com.kltn.model.Orders;
import com.kltn.repository.OrderRepository;
import com.kltn.repository.custom.CustomOrderQuery;
import com.kltn.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * Lấy tất cả đơn hàng với phân trang và filter
     * Hỗ trợ tìm kiếm theo keywords, status, userId, và khoảng thời gian
     * Sử dụng native query cho partial search với text fields
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(GetOrderRequest request, Pageable pageable) {
        Page<Orders> ordersPage;

        // Kiểm tra nếu có keywords và không phải số (cần partial search)
        if (request.getKeywords() != null && !request.getKeywords().trim().isEmpty()) {
            try {
                // Nếu keywords là số, sử dụng specification bình thường
                Long.parseLong(request.getKeywords().trim());
                Specification<Orders> specification = CustomOrderQuery.getFilterOrder(request);
                ordersPage = orderRepository.findAll(specification, pageable);
            } catch (NumberFormatException e) {
                // Nếu keywords không phải số, sử dụng native query với tất cả filter
                ordersPage = orderRepository.findOrdersWithFilters(
                        request.getKeywords().trim(),
                        request.getUserId(),
                        request.getStatus(),
                        request.getStartDate(),
                        request.getEndDate(),
                        pageable);
            }
        } else {
            // Không có keywords, sử dụng specification bình thường
            Specification<Orders> specification = CustomOrderQuery.getFilterOrder(request);
            ordersPage = orderRepository.findAll(specification, pageable);
        }

        return ordersPage.map(order -> {
            OrderDto orderDto = orderMapper.toOrderDto(order);

            // Lazy load orderItems within the transaction
            if (order.getOrderItems() != null) {
                // Force initialization of lazy collection
                order.getOrderItems().size(); // This triggers lazy loading
                List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                        .map(orderMapper::toOrderItemDto)
                        .collect(Collectors.toList());
                orderDto.setOrderItems(orderItemDtos);
            }

            return orderDto;
        });
    }

    /**
     * Lấy danh sách đơn hàng của một user với phân trang và filter
     * Chỉ trả về các đơn hàng thuộc về user được chỉ định
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByUserId(GetOrderRequest request, Pageable pageable, Long userId) {
        // Set userId trong request để filter
        request.setUserId(userId);

        // Tạo specification từ filter params
        Specification<Orders> specification = CustomOrderQuery.getFilterOrder(request);

        // Get paginated orders với filter
        Page<Orders> ordersPage = orderRepository.findAll(specification, pageable);

        return ordersPage.map(order -> {
            OrderDto orderDto = orderMapper.toOrderDto(order);

            // Lazy load orderItems within the transaction
            if (order.getOrderItems() != null) {
                // Force initialization of lazy collection
                order.getOrderItems().size(); // This triggers lazy loading
                List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                        .map(orderMapper::toOrderItemDto)
                        .collect(Collectors.toList());
                orderDto.setOrderItems(orderItemDtos);
            }

            return orderDto;
        });
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * Bao gồm thông tin các order items và sản phẩm liên quan
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Orders order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        OrderDto orderDto = orderMapper.toOrderDto(order);

        // Map order items
        if (order.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(orderMapper::toOrderItemDto)
                    .collect(Collectors.toList());
            orderDto.setOrderItems(orderItemDtos);
        }

        return orderDto;
    }

    /**
     * Lấy chi tiết đơn hàng theo ID và userId
     * Kiểm tra quyền sở hữu - chỉ trả về nếu đơn hàng thuộc về user
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAndUserId(Long id, Long userId) {
        Orders order = orderRepository.findByIdAndUserIdWithDetails(id, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id + " cho user này"));

        OrderDto orderDto = orderMapper.toOrderDto(order);

        // Map order items
        if (order.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                    .map(orderMapper::toOrderItemDto)
                    .collect(Collectors.toList());
            orderDto.setOrderItems(orderItemDtos);
        }

        return orderDto;
    }

    /**
     * Hủy đơn hàng (dành cho admin)
     * Kiểm tra trạng thái và thời gian (chỉ cho phép hủy trong vòng 24h)
     * Không cho phép hủy đơn đã giao hoặc đang vận chuyển
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        // Kiểm tra trạng thái đơn hàng đã được hủy trước đó
        if ("cancelled".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được hủy trước đó");
        }

        // Kiểm tra thời gian trước - chỉ cho phép hủy trong vòng 24h kể từ lúc đặt hàng
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderCreatedAt = order.getCreatedAt();

        if (orderCreatedAt != null) {
            Duration duration = Duration.between(orderCreatedAt, now);
            long hoursElapsed = duration.toHours();

            if (hoursElapsed > 24) {
                throw new RuntimeException("Không thể hủy đơn hàng sau 24 giờ kể từ thời gian đặt hàng. " +
                        "Đơn hàng này đã được đặt cách đây " + hoursElapsed + " giờ");
            }
        }

        // Kiểm tra trạng thái đơn hàng - chỉ không cho phép hủy đơn đã giao hoặc đang
        // vận chuyển
        if ("delivered".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã được giao");
        }

        if ("shipping".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đang trong quá trình vận chuyển");
        }

        // Cho phép hủy đơn hàng có trạng thái "pending" hoặc "paid" trong vòng 24h

        // Cập nhật trạng thái đơn hàng thành "cancelled"
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    /**
     * Hủy đơn hàng (dành cho user)
     * Kiểm tra quyền sở hữu, trạng thái và thời gian
     * Chỉ cho phép hủy đơn hàng của chính mình trong vòng 24h
     */
    @Override
    @Transactional
    public void cancelOrderByUserId(Long id, Long userId) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        // Kiểm tra xem đơn hàng có thuộc về user không
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        // Kiểm tra trạng thái đơn hàng đã được hủy trước đó
        if ("cancelled".equals(order.getStatus())) {
            throw new RuntimeException("Đơn hàng đã được hủy trước đó");
        }

        // Kiểm tra thời gian trước - chỉ cho phép hủy trong vòng 24h kể từ lúc đặt hàng
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderCreatedAt = order.getCreatedAt();

        if (orderCreatedAt != null) {
            Duration duration = Duration.between(orderCreatedAt, now);
            long hoursElapsed = duration.toHours();

            if (hoursElapsed > 24) {
                throw new RuntimeException("Không thể hủy đơn hàng sau 24 giờ kể từ thời gian đặt hàng. " +
                        "Đơn hàng này đã được đặt cách đây " + hoursElapsed + " giờ");
            }
        }

        // Kiểm tra trạng thái đơn hàng - chỉ không cho phép hủy đơn đã giao hoặc đang
        // vận chuyển
        if ("delivered".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã được giao");
        }

        if ("shipping".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đang trong quá trình vận chuyển");
        }

        // Cho phép hủy đơn hàng có trạng thái "pending" hoặc "paid" trong vòng 24h

        // Cập nhật trạng thái đơn hàng thành "cancelled"
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    /**
     * Cập nhật trạng thái đơn hàng (dành cho admin)
     * Cho phép thay đổi giữa các trạng thái: pending, paid, cancelled
     * Admin có quyền thay đổi trạng thái tự do
     */
    @Override
    @Transactional
    public void updateOrderStatus(Long id, String status) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        // Validate status - chỉ cho phép 3 trạng thái: pending, paid, cancelled
        if (!List.of("pending", "paid", "cancelled").contains(status)) {
            throw new RuntimeException("Trạng thái không hợp lệ. Chỉ cho phép: pending, paid, cancelled");
        }

        // Admin có thể thay đổi trạng thái tự do, không bị giới hạn
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }
}