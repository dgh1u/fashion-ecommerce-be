package com.kltn.repository;

import com.kltn.model.Comment;
import com.kltn.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    List<Comment> findCommentsByProductId(Long product_id);

    Page<Comment> findAllByProduct(Product product, Pageable pageable);
}
