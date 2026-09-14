package com.project.payflow.vault.service;

import com.project.payflow.common.entity.Money;
import com.project.payflow.payment.processor.dto.PaymentProcessorResponse;
import com.project.payflow.vault.dto.request.TokenizeRequest;
import com.project.payflow.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
