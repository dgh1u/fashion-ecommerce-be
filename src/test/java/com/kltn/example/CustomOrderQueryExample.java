package com.kltn.example;

import com.kltn.dto.request.GetOrderRequest;
import com.kltn.repository.custom.CustomOrderQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Ví dụ minh họa cách sử dụng CustomOrderQuery
 * 
 * File này chỉ để tham khảo, không được sử dụng trong production
 */
public class CustomOrderQueryExample {

    public void exampleUsage() {
        // 1. Tạo GetOrderRequest với các filter
        GetOrderRequest request = new GetOrderRequest();

        // Tìm kiếm theo mã đơn hàng
        request.setKeywords("12345");

        // Lọc theo user ID
        request.setUserId(1L);

        // Lọc theo status
        request.setStatus("pending");

        // Lọc theo khoảng thời gian
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        // Sắp xếp
        request.setSortField("createdAt");
        request.setSortType("DESC");

        // Pagination
        request.setStart(0);
        request.setLimit(20);

        // 2. Tạo Specification từ CustomOrderQuery
        Specification<com.kltn.model.Orders> specification = CustomOrderQuery.getFilterOrder(request);

        // 3. Tạo Pageable
        Pageable pageable = PageRequest.of(request.getStart(), request.getLimit());

        // 4. Sử dụng trong Repository (ví dụ)
        // Page<Orders> ordersPage = orderRepository.findAll(specification, pageable);

        System.out.println("Filter example created successfully!");
        System.out.println("Keywords: " + request.getKeywords());
        System.out.println("UserId: " + request.getUserId());
        System.out.println("Status: " + request.getStatus());
        System.out.println("StartDate: " + request.getStartDate());
        System.out.println("EndDate: " + request.getEndDate());
        System.out.println("SortField: " + request.getSortField());
        System.out.println("SortType: " + request.getSortType());
    }

    /**
     * Ví dụ các trường hợp filter khác nhau
     */
    public void differentFilterExamples() {

        // Ví dụ 1: Chỉ tìm kiếm theo mã đơn hàng
        GetOrderRequest searchByOrderCode = new GetOrderRequest();
        searchByOrderCode.setKeywords("DH001");

        // Ví dụ 2: Lọc đơn hàng của user cụ thể theo status
        GetOrderRequest userOrders = new GetOrderRequest();
        userOrders.setUserId(5L);
        userOrders.setStatus("confirmed");

        // Ví dụ 3: Lấy đơn hàng trong tuần qua, sắp xếp theo giá trị
        GetOrderRequest weeklyOrders = new GetOrderRequest();
        weeklyOrders.setStartDate("2024-01-20");
        weeklyOrders.setEndDate("2024-01-27");
        weeklyOrders.setSortField("totalAmount");
        weeklyOrders.setSortType("DESC");

        // Ví dụ 4: Lấy đơn hàng pending gần đây nhất
        GetOrderRequest recentPending = new GetOrderRequest();
        recentPending.setStatus("pending");
        recentPending.setSortField("createdAt");
        recentPending.setSortType("DESC");
        recentPending.setLimit(10);

        System.out.println("Different filter examples created!");
    }

    /**
     * Ví dụ validation cases
     */
    public void validationExamples() {
        GetOrderRequest request = new GetOrderRequest();

        // Các giá trị hợp lệ
        request.setStart(0); // >= 0
        request.setLimit(10); // 5-50
        request.setSortType("ASC"); // ASC hoặc DESC

        // Date format đúng
        request.setStartDate("2024-01-01"); // YYYY-MM-DD
        request.setEndDate("2024-12-31"); // YYYY-MM-DD

        // Status hợp lệ (ví dụ)
        String[] validStatuses = { "pending", "confirmed", "shipped", "delivered", "cancelled" };
        request.setStatus(validStatuses[0]);

        System.out.println("Validation examples completed!");
    }
}