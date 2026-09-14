package com.project.payflow.payment.dto.response;

import com.project.payflow.common.entity.Money;
import com.project.payflow.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record OrderResponse(

        UUID id,
        UUID merchantId,
        UUID customerId,
        String receipt,
        Money amount,
        OrderStatus status,
        Integer attempts,
        Map<String, Objects> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt

) {
}
