package com.project.payflow.payment.dto.request;

import com.project.payflow.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequestDto(

        @NotNull(message = "Order Id is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        Map<String, Object> methodDetails
) {
}
