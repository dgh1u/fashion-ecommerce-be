package com.kltn.repository;

import com.kltn.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {
        Optional<Orders> findByOrderCode(Long orderCode);

        // Query cho pagination - không fetch collections để tránh warning
        @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.user")
        Page<Orders> findAllWithDetails(Pageable pageable);

        // Query cho pagination theo userId
        @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.user WHERE o.user.id = :userId")
        Page<Orders> findAllByUserIdWithDetails(Long userId, Pageable pageable);

        @Query("SELECT o FROM Orders o " +
                        "LEFT JOIN FETCH o.user " +
                        "LEFT JOIN FETCH o.orderItems oi " +
                        "LEFT JOIN FETCH oi.product " +
                        "LEFT JOIN FETCH oi.size " +
                        "WHERE o.id = :id")
        Optional<Orders> findByIdWithDetails(Long id);

        @Query("SELECT o FROM Orders o " +
                        "LEFT JOIN FETCH o.user " +
                        "LEFT JOIN FETCH o.orderItems oi " +
                        "LEFT JOIN FETCH oi.product " +
                        "LEFT JOIN FETCH oi.size " +
                        "WHERE o.id = :id AND o.user.id = :userId")
        Optional<Orders> findByIdAndUserIdWithDetails(Long id, Long userId);

        // Native query để search orderCode partial match với tất cả filter
        @Query(value = "SELECT DISTINCT o.* FROM orders o " +
                        "LEFT JOIN users u ON o.user_id = u.id " +
                        "WHERE (:keyword IS NULL OR CAST(o.order_code AS CHAR) LIKE CONCAT('%', :keyword, '%')) " +
                        "AND (:userId IS NULL OR o.user_id = :userId) " +
                        "AND (:status IS NULL OR o.status = :status) " +
                        "AND (:startDate IS NULL OR DATE(o.created_at) >= :startDate) " +
                        "AND (:endDate IS NULL OR DATE(o.created_at) <= :endDate)", nativeQuery = true)
        Page<Orders> findOrdersWithFilters(@Param("keyword") String keyword,
                        @Param("userId") Long userId,
                        @Param("status") String status,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        Pageable pageable);
}