package com.kltn.repository;

import com.kltn.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentHistory, Long>, JpaSpecificationExecutor<PaymentHistory> {
    Optional<PaymentHistory> findByOrderCode(Long orderCode);
}
