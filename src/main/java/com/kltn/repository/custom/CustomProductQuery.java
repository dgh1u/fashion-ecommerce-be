package com.kltn.repository.custom;

import com.kltn.constant.Constant;
import com.kltn.model.Criteria;
import com.kltn.model.Size;
import com.kltn.model.Product;
import com.kltn.model.User;
import com.kltn.utils.CriteriaBuilderUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomProductQuery {

    private CustomProductQuery() {
    }

    @Data
    @NoArgsConstructor
    public static class ProductFilterParam extends CustomCriteriaQuery.CriteriaFilterParam {
        private String keywords;
        private Boolean approved;
        private Boolean notApproved;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String type;
        private Boolean del;
        private Long userId;
        private String sortField;
        private String sortType;
    }

    public static Specification<Product> getFilterProduct(ProductFilterParam param) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo title của Product
            if (param.keywords != null) {
                predicates.add(CriteriaBuilderUtil.createPredicateForSearchInsensitive(root, criteriaBuilder,
                        param.keywords, "title"));
            }

            // Lọc theo trạng thái approved và notApproved
            if (param.getApproved() != null) {
                predicates.add(criteriaBuilder.equal(root.get("approved"), param.getApproved()));
            }
            if (param.getNotApproved() != null) {
                predicates.add(criteriaBuilder.equal(root.get("notApproved"), param.getNotApproved()));
            }

            // Lọc theo trạng thái hiển thị del
            if (param.getDel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("del"), param.getDel()));
            }

            // Lọc theo ngày tạo
            if (param.getStartDate() != null && param.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createAt"),
                        param.getStartDate(), param.getEndDate()));
            } else if (param.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createAt"), param.getStartDate()));
            } else if (param.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createAt"), param.getEndDate()));
            }

            // Lọc theo userId
            if (param.getUserId() != null) {
                Join<Product, User> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), param.getUserId()));
            }

            // Nếu có bất kỳ trường lọc của Criteria nào được set, thực hiện join với
            // Criteria
            if (param.getMinPrice() != null || param.getMaxPrice() != null ||
                    param.getMinAcreage() != null || param.getMaxAcreage() != null ||
                    param.getInterior() != null || param.getKitchen() != null ||
                    param.getAirConditioner() != null || param.getHeater() != null ||
                    param.getInternet() != null || param.getOwner() != null ||
                    param.getParking() != null || param.getToilet() != null ||
                    param.getTime() != null || param.getSecurity() != null ||
                    param.getGender() != null || param.getFirstClass() != null ||
                    param.getFirstClasss() != null || // Thêm kiểm tra cho firstClasss
                    param.getOpenHours() != null || param.getSecondClass() != null ||
                    param.getDelivery() != null || param.getDineIn() != null ||
                    param.getTakeAway() != null || param.getBigSpace() != null ||
                    param.getMajor() != null ||
                    (param.getSizeName() != null && !param.getSizeName().isEmpty()) ||
                    (param.getKeywords() != null && !param.getKeywords().isEmpty())) {
                Join<Product, Criteria> criteriaJoin = root.join("criteria", JoinType.LEFT);

                // Lọc theo khoảng giá
                if (param.getMinPrice() != null && param.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.between(criteriaJoin.get("price"),
                            param.getMinPrice(), param.getMaxPrice()));
                } else if (param.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaJoin.get("price"),
                            param.getMinPrice()));
                } else if (param.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(criteriaJoin.get("price"),
                            param.getMaxPrice()));
                }

                // Lọc theo diện tích
                if (param.getMinAcreage() != null && param.getMaxAcreage() != null) {
                    predicates.add(criteriaBuilder.between(criteriaJoin.get("acreage"),
                            param.getMinAcreage(), param.getMaxAcreage()));
                } else if (param.getMinAcreage() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteriaJoin.get("acreage"),
                            param.getMinAcreage()));
                } else if (param.getMaxAcreage() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(criteriaJoin.get("acreage"),
                            param.getMaxAcreage()));
                }

                // Lọc theo các thuộc tính boolean
                if (param.getInterior() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("interior"), param.getInterior()));
                }
                if (param.getKitchen() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("kitchen"), param.getKitchen()));
                }
                if (param.getAirConditioner() != null) {
                    predicates
                            .add(criteriaBuilder.equal(criteriaJoin.get("airConditioner"), param.getAirConditioner()));
                }
                if (param.getHeater() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("heater"), param.getHeater()));
                }
                if (param.getInternet() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("internet"), param.getInternet()));
                }
                if (param.getOwner() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("owner"), param.getOwner()));
                }
                if (param.getParking() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("parking"), param.getParking()));
                }
                if (param.getToilet() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("toilet"), param.getToilet()));
                }
                if (param.getTime() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("time"), param.getTime()));
                }
                if (param.getSecurity() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("security"), param.getSecurity()));
                }
                if (param.getGender() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("gender"), param.getGender()));
                }

                // Cập nhật xử lý firstClass - hỗ trợ cả hai phương thức
                if (param.getFirstClasss() != null && !param.getFirstClasss().isEmpty()) {
                    // Sử dụng IN khi có nhiều giá trị
                    predicates.add(criteriaJoin.get("firstClass").in(param.getFirstClasss()));
                } else if (param.getFirstClass() != null) {
                    // Vẫn giữ phương thức cũ để đảm bảo tương thích ngược
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("firstClass"), param.getFirstClass()));
                }

                if (param.getOpenHours() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("openHours"), param.getOpenHours()));
                }

                if (param.getSecondClass() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("secondClass"), param.getSecondClass()));
                }

                if (param.getDelivery() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("delivery"), param.getDelivery()));
                }

                if (param.getDineIn() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("dineIn"), param.getDineIn()));
                }

                if (param.getTakeAway() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("takeAway"), param.getTakeAway()));
                }

                if (param.getBigSpace() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("bigSpace"), param.getBigSpace()));
                }

                if (param.getMajor() != null) {
                    predicates.add(criteriaBuilder.equal(criteriaJoin.get("major"), param.getMajor()));
                }

                // Lọc theo sizeName
                if (param.getSizeName() != null && !param.getSizeName().isEmpty()) {
                    Join<Criteria, Size> sizeJoin = criteriaJoin.join("size", JoinType.LEFT);
                    predicates.add(criteriaBuilder.equal(sizeJoin.get("name"), param.getSizeName()));
                }
            }

            // Xử lý sắp xếp
            if (param.sortField != null && !param.sortField.equals("")) {
                if ("price".equals(param.sortField)) {
                    // Sắp xếp theo giá - cần join với Criteria nếu chưa join
                    Join<Product, Criteria> priceJoin = root.join("criteria", JoinType.LEFT);

                    if (param.sortType.equals(Constant.SortType.DESC) || param.sortType.equals("")) {
                        // Giá cao trước (DESC)
                        query.orderBy(criteriaBuilder.desc(priceJoin.get("price")));
                    } else if (param.sortType.equals(Constant.SortType.ASC)) {
                        // Giá thấp trước (ASC)
                        query.orderBy(criteriaBuilder.asc(priceJoin.get("price")));
                    }
                } else {
                    // Sắp xếp theo các trường khác của Product
                    if (param.sortType.equals(Constant.SortType.DESC) || param.sortType.equals("")) {
                        query.orderBy(criteriaBuilder.desc(root.get(param.sortField)));
                    } else if (param.sortType.equals(Constant.SortType.ASC)) {
                        query.orderBy(criteriaBuilder.asc(root.get(param.sortField)));
                    }
                }
            } else {
                // Mặc định sắp xếp theo ID giảm dần (tin mới nhất trước)
                query.orderBy(criteriaBuilder.desc(root.get("id")));
                query.orderBy(criteriaBuilder.desc(root.get("id")));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}