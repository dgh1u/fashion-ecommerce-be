package com.kltn.repository.custom;

import com.kltn.constant.Constant;
import com.kltn.model.Comment;
import com.kltn.model.Product;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomCommentQuery {
    private CustomCommentQuery() {
    }

    @Data
    @NoArgsConstructor
    public static class CommentFilterParam {
        private String sortField;
        private String sortType;
        private Product productId;
    }

    public static Specification<Comment> getFilterComment(CommentFilterParam param) {
        return (((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo userId
            if (param.productId != null) {
                predicates.add(criteriaBuilder.equal(root.get("product"), param.productId));
            }

            if (param.sortField != null && !param.sortField.equals("")) {
                if (param.sortType.equals(Constant.SortType.DESC) || param.sortType.equals("")) {
                    query.orderBy(criteriaBuilder.desc(root.get(param.sortField)));
                }
                if (param.sortType.equals(Constant.SortType.ASC)) {
                    query.orderBy(criteriaBuilder.asc(root.get(param.sortField)));
                }
            } else {
                query.orderBy(criteriaBuilder.desc(root.get("id")));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }));
    }

}
