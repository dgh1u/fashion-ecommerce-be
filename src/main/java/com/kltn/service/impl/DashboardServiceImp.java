package com.kltn.service.impl;

import com.kltn.dto.response.dashboard.DashboardRevenueStatDTO;
import com.kltn.dto.response.dashboard.DashboardSummaryDTO;
import com.kltn.dto.response.dashboard.DashboardUserProductStatDTO;
import com.kltn.model.PaymentHistory;
import com.kltn.model.Product;
import com.kltn.model.User;
import com.kltn.repository.PaymentRepository;
import com.kltn.repository.UserRepository;
import com.kltn.repository.ProductRepository;
import com.kltn.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.AbstractMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImp implements DashboardService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Giả sử định dạng của trường transactionDateTime là "yyyy-MM-dd HH:mm:ss"
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<DashboardRevenueStatDTO> getRevenueStatistics(String start, String end, String groupBy) {
        // Xây dựng Specification để lọc theo khoảng thời gian dựa vào
        // transactionDateTime
        Specification<PaymentHistory> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (start != null && !start.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDateTime"), start));
            }
            if (end != null && !end.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDateTime"), end));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<PaymentHistory> payments = paymentRepository.findAll(spec);

        // Hàm nhóm các giao dịch theo đơn vị: day, month hoặc year
        Function<PaymentHistory, String> groupingFunction = payment -> {
            try {
                LocalDateTime ldt = LocalDateTime.parse(payment.getTransactionDateTime(), dateTimeFormatter);
                switch (groupBy.toLowerCase()) {
                    case "month":
                        // vd: "2023-05"
                        return String.format("%d-%02d", ldt.getYear(), ldt.getMonthValue());
                    case "year":
                        // vd: "2023"
                        return String.valueOf(ldt.getYear());
                    case "day":
                    default:
                        // vd: "2023-05-28"
                        return ldt.toLocalDate().toString();
                }
            } catch (Exception e) {
                log.error("Lỗi chuyển đổi transactionDateTime: {}", payment.getTransactionDateTime(), e);
                return "Other";
            }
        };

        Map<String, List<PaymentHistory>> grouped = payments.stream()
                .collect(Collectors.groupingBy(groupingFunction));

        List<DashboardRevenueStatDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<PaymentHistory>> entry : grouped.entrySet()) {
            String groupKey = entry.getKey();
            List<PaymentHistory> list = entry.getValue();
            long transactionCount = list.size();
            long totalRevenue = list.stream()
                    .filter(PaymentHistory::isSuccess)
                    .mapToLong(ph -> ph.getAmount() != null ? ph.getAmount() : 0)
                    .sum();
            result.add(new DashboardRevenueStatDTO(groupKey, transactionCount, totalRevenue));
        }

        // Sắp xếp theo groupKey (nếu cần, theo thứ tự thời gian)
        result.sort(Comparator.comparing(DashboardRevenueStatDTO::getGroupKey));
        return result;
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary() {
        long totalUsers = userRepository.count();
        long totalPayments = paymentRepository.count();
        long totalRevenue = paymentRepository.findAll().stream()
                .filter(PaymentHistory::isSuccess)
                .mapToLong(ph -> ph.getAmount() != null ? ph.getAmount() : 0)
                .sum();
        long totalProducts = productRepository.count();

        return new DashboardSummaryDTO(totalUsers, totalPayments, totalProducts, totalRevenue);
    }

    @Override
    public List<DashboardUserProductStatDTO> getUserProductStatistics(String start, String end, String groupBy) {
        // Specification cho Product
        Specification<Product> productSpec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (start != null && !start.isEmpty()) {
                // Convert String to LocalDateTime for start date
                LocalDateTime startDate = LocalDate.parse(start)
                        .atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createAt"), startDate));
            }

            if (end != null && !end.isEmpty()) {
                // Convert String to LocalDateTime for end date (end of day)
                LocalDateTime endDate = LocalDate.parse(end)
                        .atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createAt"), endDate));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<Product> products = productRepository.findAll(productSpec);

        // Nhóm products theo thời gian
        Map<String, Long> productGroups = products.stream()
                .collect(Collectors.groupingBy(product -> {
                    LocalDateTime ldt = product.getCreateAt();
                    switch (groupBy.toLowerCase()) {
                        case "month":
                            return String.format("%d-%02d", ldt.getYear(), ldt.getMonthValue());
                        case "year":
                            return String.valueOf(ldt.getYear());
                        case "day":
                        default:
                            return ldt.toLocalDate().toString();
                    }
                }, Collectors.counting()));

        // Chuyển đổi kết quả
        List<DashboardUserProductStatDTO> result = productGroups.entrySet().stream()
                .map(entry -> new DashboardUserProductStatDTO(
                        entry.getKey(),
                        entry.getValue()))
                .sorted(Comparator.comparing(DashboardUserProductStatDTO::getGroupKey))
                .collect(Collectors.toList());

        return result;
    }
}
