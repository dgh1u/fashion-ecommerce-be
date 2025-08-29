package com.kltn.repository.custom;

import com.kltn.model.Criteria;
import com.kltn.model.Size;
import com.kltn.model.Product;
import com.kltn.utils.CriteriaBuilderUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomCriteriaQuery {

    private CustomCriteriaQuery() {
    }

    @Data
    @NoArgsConstructor
    public static class CriteriaFilterParam {
        private String keywords;
        private Double minAcreage;
        private Double maxAcreage;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String sizeName;
        private Boolean interior;
        private Boolean kitchen;
        private Boolean airConditioner;
        private Boolean heater;
        private Boolean internet;
        private Boolean owner;
        private Boolean parking;
        private Boolean toilet;
        private Boolean time;
        private Boolean security;
        private Boolean gender;
        private String firstClass;
        // Thêm mới: danh sách các giá trị firstClass
        private List<String> firstClasss;

        private String openHours;
        private String secondClass;
        private Boolean delivery;
        private Boolean dineIn;
        private Boolean takeAway;
        private Boolean bigSpace;
        private String major;
    }

    public static Specification<Criteria> getFilterCriteria(CriteriaFilterParam param) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Criteria, Size> sizeJoin = root.join("size");
            Join<Criteria, Product> productJoin = root.join("product", JoinType.LEFT);

            // Lọc theo tiêu đề bài đăng
            if (param.getKeywords() != null) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(
                        productJoin, criteriaBuilder, param.getKeywords(), "title"));
            }

            if (param.getSizeName() != null && !param.getSizeName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(sizeJoin.get("name"), param.getSizeName()));
            }

            // Lọc theo khoảng giá
            if (param.getMinPrice() != null && param.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.between(root.get("price"), param.getMinPrice(), param.getMaxPrice()));
            } else if (param.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), param.getMinPrice()));
            } else if (param.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), param.getMaxPrice()));
            }

            // Lọc theo diện tích
            if (param.getMinAcreage() != null && param.getMaxAcreage() != null) {
                predicates.add(
                        criteriaBuilder.between(root.get("acreage"), param.getMinAcreage(), param.getMaxAcreage()));
            } else if (param.getMinAcreage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("acreage"), param.getMinAcreage()));
            } else if (param.getMaxAcreage() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("acreage"), param.getMaxAcreage()));
            }

            // Lọc theo khu vực
            if (param.getSizeName() != null && !param.getSizeName().isEmpty()) {
                predicates.add(criteriaBuilder.equal(sizeJoin.get("name"), param.getSizeName()));
            }

            // Lọc theo đặc điểm boolean
            if (param.getInterior() != null) {
                predicates.add(criteriaBuilder.equal(root.get("interior"), param.getInterior()));
            }
            if (param.getKitchen() != null) {
                predicates.add(criteriaBuilder.equal(root.get("kitchen"), param.getKitchen()));
            }
            if (param.getAirConditioner() != null) {
                predicates.add(criteriaBuilder.equal(root.get("airConditioner"), param.getAirConditioner()));
            }
            if (param.getHeater() != null) {
                predicates.add(criteriaBuilder.equal(root.get("heater"), param.getHeater()));
            }
            if (param.getInternet() != null) {
                predicates.add(criteriaBuilder.equal(root.get("internet"), param.getInternet()));
            }
            if (param.getOwner() != null) {
                predicates.add(criteriaBuilder.equal(root.get("owner"), param.getOwner()));
            }
            if (param.getParking() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parking"), param.getParking()));
            }
            if (param.getToilet() != null) {
                System.out.println("Toilet field type: " + root.get("toilet").getJavaType());
                predicates.add(criteriaBuilder.equal(root.get("toilet"), param.getToilet()));
            }
            if (param.getTime() != null) {
                predicates.add(criteriaBuilder.equal(root.get("time"), param.getTime()));
            }
            if (param.getSecurity() != null) {
                predicates.add(criteriaBuilder.equal(root.get("security"), param.getSecurity()));
            }
            if (param.getGender() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), param.getGender()));
            }

            // Cập nhật xử lý firstClass - hỗ trợ cả hai phương thức
            if (param.getFirstClasss() != null && !param.getFirstClasss().isEmpty()) {
                // Sử dụng IN khi có nhiều giá trị
                predicates.add(root.get("firstClass").in(param.getFirstClasss()));
            } else if (param.getFirstClass() != null) {
                // Vẫn giữ phương thức cũ để đảm bảo tương thích ngược
                predicates.add(criteriaBuilder.equal(root.get("firstClass"), param.getFirstClass()));
            }

            if (param.getSecondClass() != null) {
                predicates.add(criteriaBuilder.equal(root.get("secondClass"), param.getSecondClass()));
            }
            if (param.getOpenHours() != null) {
                predicates.add(criteriaBuilder.equal(root.get("openHours"), param.getOpenHours()));
            }

            if (param.getDelivery() != null) {
                predicates.add(criteriaBuilder.equal(root.get("delivery"), param.getDelivery()));
            }

            if (param.getDineIn() != null) {
                predicates.add(criteriaBuilder.equal(root.get("dineIn"), param.getDineIn()));
            }

            if (param.getTakeAway() != null) {
                predicates.add(criteriaBuilder.equal(root.get("takeAway"), param.getTakeAway()));
            }

            if (param.getBigSpace() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bigSpace"), param.getBigSpace()));
            }
            if (param.getMajor() != null) {
                predicates.add(criteriaBuilder.equal(root.get("major"), param.getMajor()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}