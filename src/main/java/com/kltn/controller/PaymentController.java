package com.kltn.controller;

import com.kltn.config.JwtConfig;
import com.kltn.dto.request.payment.GetPaymentRequest;
import com.kltn.dto.request.payment.CreatePaymentRequest;
import com.kltn.dto.request.payment.PaymentReceiveHookRequest;
import com.kltn.dto.response.BaseResponse;
import com.kltn.dto.response.payment.CreatePaymentResponse;
import com.kltn.exception.DataExistException;
import com.kltn.mapper.PaymentMapper;
import com.kltn.model.PaymentHistory;
import com.kltn.repository.PaymentRepository;
import com.kltn.service.PaymentService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.CheckoutResponseData;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtConfig jwtConfig;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String token) throws Exception {
        String jwt = token.substring(7);
        Claims claims = jwtConfig.getClaims(jwt);
        System.out.println("cliams: " + claims);
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            Long userId = Long.parseLong(userIdObj.toString());

            CheckoutResponseData data = paymentService.createPayment(request, userId);
            String checkoutUrl = data.getCheckoutUrl();
            return ResponseEntity.ok(CreatePaymentResponse.builder().url(checkoutUrl).build());
        }
        throw new DataExistException("Thanh toán thất bại");
    }

    // https://464d-2402-800-619d-1df1-f009-e0b5-cdc7-1b12.ngrok-free.app/api/payment/receive-hook
    @PostMapping("/receive-hook")
    public ResponseEntity<?> receiveHook(@RequestBody PaymentReceiveHookRequest request) throws Exception {
        paymentService.receiveHook(request);
        return ResponseEntity.ok("Chuyển khoản paypos");
    }

    @ApiOperation(value = "Lấy lịch sử giao dịch")
    @GetMapping("")
    public ResponseEntity<?> getAllPayment(@Valid @ModelAttribute GetPaymentRequest request) {
        Page<PaymentHistory> page = paymentService.getAllPayment(request,
                PageRequest.of(request.getStart(), request.getLimit()));

        return BaseResponse.successListData(page.getContent().stream()
                .map(paymentMapper::toPaymentDTO)
                .collect(Collectors.toList()), (int) page.getTotalElements());
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<?> getPaymentResult(@PathVariable Long id) {
        Optional<PaymentHistory> paymentOpt = paymentRepository.findById(id);
        if (!paymentOpt.isPresent()) {
            return ResponseEntity.status(404).body("Giao dịch không tồn tại");
        }
        PaymentHistory paymentHistory = paymentOpt.get();
        return ResponseEntity.ok(paymentHistory);
    }

}
