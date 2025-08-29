// src/main/java/com/nckh/firstClassroom/service/DocumentService.java
package com.kltn.service;

import com.kltn.dto.entity.DocumentDto;
import com.kltn.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentDto uploadDocument(Long idProduct, MultipartFile file);

    Document getDocument(String documentId);

    List<String> getDocumentsByIdProduct(Long idProduct);

    void deleteSingleDocument(String documentId);

    List<DocumentDto> getDocumentDTOsByIdProduct(Long idProduct);

    Document getDocumentForDownload(String documentId);
}