package com.kltn.service;

import com.kltn.dto.request.payment.CreatePaymentRequest;
import com.kltn.dto.request.payment.PaymentReceiveHookRequest;
import com.kltn.model.PaymentHistory;
import com.kltn.repository.custom.CustomPaymentQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

public interface PaymentService {
    CreatePaymentLinkResponse createPayment(CreatePaymentRequest request, Long id);

    CreatePaymentLinkResponse createPaymentLinkForOrder(CreatePaymentRequest request, Long userId, Long orderId);

    void receiveHook(PaymentReceiveHookRequest request);

    Page<PaymentHistory> getAllPayment(CustomPaymentQuery.PaymentFilterParam param, PageRequest pageRequest);

}
