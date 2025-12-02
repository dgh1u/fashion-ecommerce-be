package com.kltn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity(name = "payment_history")
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "code")
    private int code;

    @Column(name = "descrip")
    private String descrip;

    @Column(name = "desc_status")
    private String descStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "success")
    private boolean success;

    @Column(name = "payment_link_id")
    private String paymentLinkId;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "order_code")
    private Long orderCode;

    @Column(name = "price")
    private Integer price;

    @Column(name = "reference")
    private String reference;

    @Column(name = "transaction_date_time")
    private String transactionDateTime;

    @Column(name = "currency")
    private String currency;

    @Column(name = "counter_account_bank_id")
    private String counterAccountBankId;

    @Column(name = "counter_account_bank_name")
    private String counterAccountBankName;

    @Column(name = "counter_account_name")
    private String counterAccountName;

    @Column(name = "counter_account_number")
    private String counterAccountNumber;

    @Column(name = "virtual_account_name")
    private String virtualAccountName;

    @Column(name = "virtual_account_number")
    private String virtualAccountNumber;

    @Column(name = "signature")
    private String signature;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Orders order;
}
