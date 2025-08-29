// src/main/java/com/nckh/firstClassroom/service/impl/DocumentServiceImpl.java
package com.kltn.service.impl;

import com.kltn.dto.entity.DocumentDto;
import com.kltn.exception.DataNotFoundException;
import com.kltn.exception.MyCustomException;
import com.kltn.mapper.DocumentMapper;
import com.kltn.model.Document;
import com.kltn.model.Product;
import com.kltn.repository.DocumentRepository;
import com.kltn.repository.ProductRepository;
import com.kltn.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentRepository documentRepository;
    private final ProductRepository productRepository;

    // Các định dạng file được phép
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".docx", ".ppt", ".pptx", ".xlsx",
            ".zip");

    @Override
    public DocumentDto uploadDocument(Long idProduct, MultipartFile file) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            // Kiểm tra định dạng file
            String fileName = file.getOriginalFilename();
            if (!isValidFileType(fileName)) {
                throw new MyCustomException("Chỉ cho phép upload file .pdf, .docx, .ppt, .pptx, .xlsx ");
            }

            Document document = storeDocument(idProduct, file);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/document/")
                    .path(document.getId())
                    .toUriString();
            return new DocumentDto(document.getId(), document.getFileName(),
                    file.getContentType(), fileDownloadUri, idProduct);
        } else {
            throw new DataNotFoundException("Không tìm thấy bài đăng có ID " + idProduct);
        }
    }

    @Override
    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài liệu có id " + documentId));
    }

    @Override
    public List<String> getDocumentsByIdProduct(Long idProduct) {
        List<String> uri = new ArrayList<>();
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            List<Document> documents = documentRepository.findDocumentByProduct(product.get());
            for (Document document : documents) {
                uri.add(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/document/")
                        .path(document.getId())
                        .toUriString());
            }
        }
        return uri;
    }

    @Override
    public void deleteSingleDocument(String documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isPresent()) {
            documentRepository.delete(document.get());
        } else {
            throw new DataNotFoundException("Không tìm thấy tài liệu có ID " + documentId);
        }
    }

    @Override
    public List<DocumentDto> getDocumentDTOsByIdProduct(Long idProduct) {
        Optional<Product> product = productRepository.findById(idProduct);
        if (product.isPresent()) {
            List<Document> documents = documentRepository.findDocumentByProduct(product.get());
            List<DocumentDto> documentDtos = new ArrayList<>();
            for (Document document : documents) {
                DocumentDto documentDto = documentMapper.toDto(document);
                documentDto.setUri(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/document/")
                        .path(document.getId())
                        .toUriString());
                documentDtos.add(documentDto);
            }
            return documentDtos;
        } else {
            throw new DataNotFoundException("Không tìm thấy bài đăng có ID " + idProduct);
        }
    }

    private Document storeDocument(Long idProduct, MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new DataNotFoundException("Tên file không hợp lệ " + fileName);
            }
            Optional<Product> product = productRepository.findById(idProduct);
            Document document = new Document(fileName, file.getContentType(), file.getBytes(), product.get());
            return documentRepository.save(document);
        } catch (Exception e) {
            throw new MyCustomException("Lỗi khi lưu tài liệu " + fileName + "!");
        }
    }

    private boolean isValidFileType(String fileName) {
        if (fileName == null)
            return false;
        String lowerFileName = fileName.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
    }

    @Override
    public Document getDocumentForDownload(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài liệu có id " + documentId));
    }
}