// src/main/java/com/nckh/firstClassroom/controller/DocumentController.java
package com.kltn.controller;

import com.kltn.dto.entity.DocumentDto;
import com.kltn.dto.response.BaseResponse;
import com.kltn.model.Document;
import com.kltn.service.DocumentService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @ApiOperation(value = "Upload tài liệu cho bài đăng")
    @PostMapping("/document/upload/{productId}")
    public ResponseEntity<?> uploadDocument(@PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        try {
            DocumentDto documentDto = documentService.uploadDocument(productId, file);
            return BaseResponse.successData(documentDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ApiOperation(value = "Tải tài liệu về máy")
    @GetMapping("/document/download/{documentId}")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable String documentId) {
        try {
            Document document = documentService.getDocumentForDownload(documentId);

            ByteArrayResource resource = new ByteArrayResource(document.getData());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .contentLength(document.getData().length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ApiOperation(value = "Lấy danh sách tài liệu của bài đăng")
    @GetMapping("/documents/{productId}")
    public ResponseEntity<?> getDocumentsByProduct(@PathVariable Long productId) {
        try {
            List<DocumentDto> documents = documentService.getDocumentDTOsByIdProduct(productId);
            return BaseResponse.successData(documents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @ApiOperation(value = "Xóa một tài liệu cụ thể")
    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<?> deleteSingleDocument(@PathVariable String documentId) {
        try {
            documentService.deleteSingleDocument(documentId);
            return BaseResponse.successData("Đã xóa tài liệu thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}