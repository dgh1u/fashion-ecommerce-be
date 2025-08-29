package com.kltn.repository;

import com.kltn.model.Image;
import com.kltn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, String> {
    List<Image> findImageByProduct(Product product);
}
