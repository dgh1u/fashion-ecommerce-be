package com.kltn.repository;

import com.kltn.model.Product;
import com.kltn.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByUser(User user, Pageable pageable);

    Optional<Product> findProductById(Long id);

    Page<Product> findAllByUser_EmailAndDel(String email, boolean del, Pageable page);
    // Phương thức tìm bài đăng nhà nguyên căn với các điều kiện lọc
}
