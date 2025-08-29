package com.kltn.service;

import com.kltn.model.Action;
import com.kltn.model.Product;
import com.kltn.model.User;
import com.kltn.model.enums.ActionName;
import com.kltn.repository.custom.CustomActionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public interface ActionService {
    void createAction(Product product, User user, ActionName actionName);

    Page<Action> getAction(CustomActionQuery.ActionFilterParam param, PageRequest pageRequest);

    void markActionAsRead(Long actionId);

}
