package com.project.payflow.payment.controller;

import com.project.payflow.merchant.security.MerchantContext;
import com.project.payflow.payment.dto.request.PaymentInitRequestDto;
import com.project.payflow.payment.dto.response.PaymentResponse;
import com.project.payflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantContext merchantContext;

    // UUID merchantId = UUID.fromString("");

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequestDto request){
       return ResponseEntity.status(HttpStatus.CREATED)
               .body(paymentService.initiate(merchantContext.getMerchantId(), request));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId){
        return ResponseEntity.ok(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }

}
