package com.kltn.service.impl;

import com.kltn.exception.DataNotFoundException;
import com.kltn.model.Criteria;
import com.kltn.repository.CriteriaRepository;
import com.kltn.repository.custom.CustomCriteriaQuery;
import com.kltn.service.CriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CriteriaServiceImp implements CriteriaService {

    private final CriteriaRepository criteriaRepository;

    @Override
    public Page<Criteria> getAllCriteria(CustomCriteriaQuery.CriteriaFilterParam param, PageRequest pageRequest) {
        try {
            Specification<Criteria> specification = CustomCriteriaQuery.getFilterCriteria(param);
            return criteriaRepository.findAll(specification, pageRequest);
        }catch (Exception e){
            throw new DataNotFoundException("Không có criteria nào được tìm thấy! " + e.getMessage());
        }
    }
}
