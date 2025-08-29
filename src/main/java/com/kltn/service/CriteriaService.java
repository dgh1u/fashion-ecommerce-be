package com.kltn.service;

import com.kltn.model.Criteria;
import com.kltn.repository.custom.CustomCriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public interface CriteriaService {
    Page<Criteria> getAllCriteria(CustomCriteriaQuery.CriteriaFilterParam param, PageRequest pageRequest);
}
