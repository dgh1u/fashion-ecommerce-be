package com.kltn.service;

import com.kltn.dto.response.dashboard.DashboardRevenueStatDTO;
import com.kltn.dto.response.dashboard.DashboardSummaryDTO;
import com.kltn.dto.response.dashboard.DashboardUserProductStatDTO;

import java.util.List;

public interface DashboardService {
    /**
     * Lấy thống kê doanh thu và số giao dịch theo đơn vị: day, month hoặc year.
     * Tham số start và end có định dạng chuỗi (ví dụ: yyyy-MM-dd) để lọc theo thời
     * gian dựa trên trường transactionDateTime.
     */
    List<DashboardRevenueStatDTO> getRevenueStatistics(String start, String end, String groupBy);

    /**
     * Lấy thống kê tổng hợp: tổng số người dùng, tổng số giao dịch, tổng số bài
     * viết, tổng doanh thu.
     */
    DashboardSummaryDTO getDashboardSummary();

    /**
     * Lấy thống kê số lượng người dùng và bài viết theo đơn vị: day, month hoặc
     * year.
     * Tham số start và end có định dạng chuỗi (ví dụ: yyyy-MM-dd) để lọc theo thời
     * gian.
     */
    List<DashboardUserProductStatDTO> getUserProductStatistics(String start, String end, String groupBy);
}
