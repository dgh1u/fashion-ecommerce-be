package com.kltn.service;

import com.kltn.dto.request.checkout.CheckoutRequest;
import com.kltn.dto.response.payment.CreatePaymentResponse;

public interface CheckoutService {
    CreatePaymentResponse checkout(Long userId, CheckoutRequest request);
}