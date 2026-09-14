package com.project.payflow.payment.service;

import com.project.payflow.payment.dto.request.PaymentInitRequestDto;
import com.project.payflow.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiate(UUID merchantId, PaymentInitRequestDto request);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, Object errorCode, Object errorDescription);
}
