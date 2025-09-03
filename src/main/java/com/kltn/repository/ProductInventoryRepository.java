package com.kltn.repository;

import com.kltn.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {
    
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId")
    List<ProductInventory> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId AND pi.size.id = :sizeId")
    Optional<ProductInventory> findByProductIdAndSizeId(@Param("productId") Long productId, @Param("sizeId") Long sizeId);
    
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id IN :productIds")
    List<ProductInventory> findByProductIdIn(@Param("productIds") List<Long> productIds);
}
