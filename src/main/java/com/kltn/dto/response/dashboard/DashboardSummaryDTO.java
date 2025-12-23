package com.kltn.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {
    // Tổng số người dùng
    private long totalUsers;
    // Tổng số giao dịch (tất cả giao dịch trong PaymentHistory)
    private long totalPayments;
    // Tổng số bài viết (dựa trên ProductRepository)
    private long totalProducts;
    // Tổng doanh thu = tổng amount của các giao dịch có success = true
    private long totalRevenue;
    // Tổng số đơn hàng
    private long totalOrders;
    // Tổng số tồn kho (tổng quantity của tất cả ProductInventory)
    private long totalInventory;
}
