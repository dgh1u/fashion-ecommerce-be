package com.kltn.repository;

import com.kltn.model.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "LEFT JOIN FETCH oi.size s " +
            "WHERE oi.order.id = :orderId")
    List<OrderItems> findByOrderIdWithProductAndSize(@Param("orderId") Long orderId);
}