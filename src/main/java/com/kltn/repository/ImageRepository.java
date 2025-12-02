package com.kltn.repository;

import com.kltn.model.Image;
import com.kltn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, String> {
    @Query("SELECT i FROM Image i WHERE i.product = :product ORDER BY i.orderIndex ASC")
    List<Image> findImageByProduct(@Param("product") Product product);
    
    @Query("SELECT COALESCE(MAX(i.orderIndex), -1) FROM Image i WHERE i.product = :product")
    Integer findMaxOrderIndexByProduct(@Param("product") Product product);
}
