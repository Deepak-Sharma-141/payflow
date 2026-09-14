package com.project.payflow.payment.gateway;

import com.project.payflow.payment.gateway.dto.PaymentRequest;
import com.project.payflow.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate(PaymentRequest request);

    PaymentResult capture(UUID paymentId);
}
