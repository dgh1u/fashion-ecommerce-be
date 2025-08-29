package com.kltn.service.impl;

import com.kltn.exception.DataNotFoundException;
import com.kltn.model.Action;
import com.kltn.model.Product;
import com.kltn.model.User;
import com.kltn.model.enums.ActionName;
import com.kltn.repository.ActionRepository;
import com.kltn.repository.custom.CustomActionQuery;
import com.kltn.service.ActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionServiceImp implements ActionService {

    private final ActionRepository actionRepository;

    @Override
    public void createAction(Product product, User user, ActionName actionName) {
        try {
            Action action = new Action(product, user, actionName);
            actionRepository.save(action);
        } catch (Exception e) {
            log.error("Lỗi khi tạo hoạt động: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo hoạt động: " + e.getMessage());
        }
    }

    @Override
    public Page<Action> getAction(CustomActionQuery.ActionFilterParam param, PageRequest pageRequest) {
        try {
            Specification<Action> specification = CustomActionQuery.getFilterAction(param);
            return actionRepository.findAll(specification, pageRequest);
        } catch (Exception e) {
            throw new DataNotFoundException("Không có bài viết nào được tìm thấy! " + e.getMessage());
        }
    }

    @Override
    public void markActionAsRead(Long actionId) {
        Optional<Action> actionOpt = actionRepository.findById(actionId);
        if (actionOpt.isPresent()) {
            Action action = actionOpt.get();
            action.setIsRead(true);
            actionRepository.save(action);
        } else {
            throw new DataNotFoundException("Không tìm thấy hoạt động với id: " + actionId);
        }
    }

}
