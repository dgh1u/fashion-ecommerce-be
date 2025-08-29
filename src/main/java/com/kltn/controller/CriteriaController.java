package com.kltn.controller;

import com.kltn.dto.request.criteria.GetCriteriaRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.mapper.CriteriaMapper;
import com.kltn.model.Criteria;
import com.kltn.service.CriteriaService;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CriteriaController {
    private final CriteriaService criteriaService;
    private final CriteriaMapper criteriaMapper;

    // hoàn thành
    @ApiOperation(value = "Lấy tất cả")
    @GetMapping("/criterias")
    public ResponseEntity<?> getAllCriteria(@Valid @ModelAttribute GetCriteriaRequest request) {
        Page<Criteria> page = criteriaService.getAllCriteria(request, PageRequest.of(request.getStart(), request.getLimit()));
        return BaseResponse.successListData(page.getContent().stream().map(criteriaMapper::toCriteriaDto).collect(Collectors.toList()), (int) page.getTotalElements());
    }

}