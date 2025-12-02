package com.kltn.service;

import com.kltn.model.Orders;

public interface InventoryService {
    void updateInventoryAfterPayment(Orders order);
}