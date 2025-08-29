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

    Page<Product> findAllByApprovedAndNotApprovedAndDel(boolean approved, boolean notApproved, boolean del,
            Pageable pageable);

    Page<Product> findAllByApprovedAndNotApproved(boolean approved, boolean notApproved, Pageable pageable);

    Page<Product> findAllByUser_EmailAndDelAndApproved(String email, boolean del, boolean approved, Pageable page);

    // Phương thức truy vấn các bài đăng chưa duyệt
    Page<Product> findByApprovedFalseAndNotApprovedFalse(Pageable pageable);
    // Phương thức tìm bài đăng nhà nguyên căn với các điều kiện lọc
}
