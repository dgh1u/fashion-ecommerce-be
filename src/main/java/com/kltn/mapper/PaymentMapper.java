package com.kltn.mapper;

import com.kltn.dto.entity.Payment;

import com.kltn.model.PaymentHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPaymentDTO(PaymentHistory paymentHistory);
}