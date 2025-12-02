package com.kltn.repository;

import com.kltn.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<PaymentHistory, Long>, JpaSpecificationExecutor<PaymentHistory> {
    Optional<PaymentHistory> findByOrderCode(Long orderCode);

    @Query("SELECT p FROM payment_history p LEFT JOIN FETCH p.order WHERE p.orderCode = :orderCode")
    Optional<PaymentHistory> findByOrderCodeWithOrder(@Param("orderCode") Long orderCode);

    @Query("SELECT DISTINCT p FROM payment_history p LEFT JOIN FETCH p.order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product pr " +
            "LEFT JOIN FETCH oi.size " +
            "WHERE p.id = :id")
    Optional<PaymentHistory> findByIdWithOrder(@Param("id") Long id);
}
