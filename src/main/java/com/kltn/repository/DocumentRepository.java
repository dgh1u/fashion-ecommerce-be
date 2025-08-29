// src/main/java/com/nckh/firstClassroom/repository/DocumentRepository.java
package com.kltn.repository;

import com.kltn.model.Document;
import com.kltn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findDocumentByProduct(Product product);
}