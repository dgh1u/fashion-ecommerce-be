package com.kltn.repository.custom;

import com.kltn.constant.Constant;
import com.kltn.model.Orders;
import com.kltn.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomOrderQuery {
    private CustomOrderQuery() {
    }

    @Data
    @NoArgsConstructor
    public static class OrderFilterParam {
        private String keywords; // cho mã đơn hàng
        private String sortField;
        private String sortType;
        private Long userId;
        private String status;
        private String startDate;
        private String endDate;
    }

    public static Specification<Orders> getFilterOrder(OrderFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo mã đơn hàng (keywords) - hỗ trợ partial search
            if (param.keywords != null && !param.keywords.trim().isEmpty()) {
                String keyword = param.keywords.trim();
                // Chuyển orderCode sang string để có thể tìm kiếm partial match
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(criteriaBuilder.toString(root.get("orderCode"))),
                        "%" + keyword.toLowerCase() + "%"));
            }

            // Lọc theo userId
            if (param.userId != null) {
                Join<Orders, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), param.userId));
            }

            // Lọc theo status
            if (param.status != null && !param.status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), param.status));
            }

            // Lọc theo ngày tạo
            if (param.startDate != null && !param.startDate.trim().isEmpty()) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", String.class, root.get("createdAt")),
                        param.startDate));
            }
            if (param.endDate != null && !param.endDate.trim().isEmpty()) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", String.class, root.get("createdAt")),
                        param.endDate));
            }

            // Sắp xếp
            if (query != null) {
                if (param.sortField != null && !param.sortField.equals("")) {
                    if (param.sortType != null && param.sortType.equals(Constant.SortType.ASC)) {
                        query.orderBy(criteriaBuilder.asc(root.get(param.sortField)));
                    } else {
                        query.orderBy(criteriaBuilder.desc(root.get(param.sortField)));
                    }
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get("id")));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}