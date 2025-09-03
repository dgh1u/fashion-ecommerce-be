package com.kltn.controller;

import com.kltn.dto.entity.SizeDto;
import com.kltn.dto.response.BaseResponse;
import com.kltn.model.Size;
import com.kltn.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SizeController {
    
    private final SizeRepository sizeRepository;
    
    @GetMapping("/sizes")
    public ResponseEntity<?> getAllSizes() {
        try {
            List<SizeDto> sizes = sizeRepository.findAll()
                    .stream()
                    .map(size -> new SizeDto(size.getId(), size.getName()))
                    .collect(Collectors.toList());
            return BaseResponse.successData(sizes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách size: " + e.getMessage());
        }
    }
}
