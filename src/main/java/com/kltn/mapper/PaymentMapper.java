package com.kltn.mapper;

import com.kltn.dto.entity.Payment;
import com.kltn.model.PaymentHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "desc", source = "descrip")
    @Mapping(target = "success", source = "success")
    @Mapping(target = "orderCode", source = "orderCode")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "descStatus", source = "descStatus")
    @Mapping(target = "accountNumber", source = "accountNumber")
    @Mapping(target = "reference", source = "reference")
    @Mapping(target = "transactionDateTime", source = "transactionDateTime")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "paymentLinkId", source = "paymentLinkId")
    @Mapping(target = "counterAccountBankId", source = "counterAccountBankId")
    @Mapping(target = "counterAccountBankName", source = "counterAccountBankName")
    @Mapping(target = "counterAccountName", source = "counterAccountName")
    @Mapping(target = "counterAccountNumber", source = "counterAccountNumber")
    @Mapping(target = "virtualAccountName", source = "virtualAccountName")
    @Mapping(target = "virtualAccountNumber", source = "virtualAccountNumber")
    @Mapping(target = "signature", source = "signature")
    @Mapping(target = "userId", source = "userId")
    Payment toPaymentDTO(PaymentHistory paymentHistory);
}