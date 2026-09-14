package com.project.payflow.payment.dto.request;

import com.project.payflow.common.entity.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public record CreateOrderRequest(

        @NotNull(message = "Amount is required")
        Money amount,

        @Size(max = 100)
        String receipt,

        Map<String, Objects> notes,

        LocalDateTime expiresAt,

        @Valid
        CustomerDetails customer
) {
      public record CustomerDetails(
              @Size(max = 200)
              String name,

              @Email
              @Size(max = 200)
              String email,

              @Size(max = 20)
              String phone

      )  {}
}
