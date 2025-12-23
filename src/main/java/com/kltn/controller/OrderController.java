package com.kltn.controller;

import com.kltn.dto.custom.CustomUserDetails;
import com.kltn.dto.entity.OrderDto;
import com.kltn.dto.request.GetOrderRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.service.OrderService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation(value = "Lấy tất cả lịch sử đơn hàng (Admin)")
    @GetMapping("/admin/orders")

    public ResponseEntity<?> getAllOrdersForAdmin(@Valid @ModelAttribute GetOrderRequest request) {
        Page<OrderDto> page = orderService.getAllOrders(request,
                PageRequest.of(request.getStart(), request.getLimit(), Sort.by("id").descending()));

        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy lịch sử đơn hàng của user đang đăng nhập")
    @GetMapping("/orders")
    public ResponseEntity<?> getOrdersForCurrentUser(@Valid @ModelAttribute GetOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        Page<OrderDto> page = orderService.getOrdersByUserId(request,
                PageRequest.of(request.getStart(), request.getLimit(), Sort.by("id").descending()), userId);

        return BaseResponse.successListData(page.getContent(), (int) page.getTotalElements());
    }

    @ApiOperation(value = "Lấy thông tin chi tiết một đơn hàng (Admin)")
    @GetMapping("/admin/order/{id}")

    public ResponseEntity<?> getOrderByIdForAdmin(@PathVariable Long id) {
        try {
            OrderDto orderDto = orderService.getOrderById(id);
            return BaseResponse.successData(orderDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Lấy thông tin chi tiết một đơn hàng của user đang đăng nhập")
    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrderByIdForCurrentUser(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            OrderDto orderDto = orderService.getOrderByIdAndUserId(id, userId);
            return BaseResponse.successData(orderDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Hủy đơn hàng (Admin)")
    @PatchMapping("/admin/order/{id}/cancel")

    public ResponseEntity<?> cancelOrderForAdmin(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return BaseResponse.successData("Hủy đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Hủy đơn hàng của user đang đăng nhập")
    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<?> cancelOrderForCurrentUser(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            orderService.cancelOrderByUserId(id, userId);
            return BaseResponse.successData("Hủy đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @ApiOperation(value = "Cập nhật trạng thái đơn hàng (Admin)")
    @PatchMapping("/admin/order/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return BaseResponse.successData("Cập nhật trạng thái đơn hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}