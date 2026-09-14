package com.project.payflow.vault.dto.response;

import com.project.payflow.common.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear

) {
}
