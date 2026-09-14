package com.project.payflow.merchant.dto.response;

import com.project.payflow.common.enums.Enviroment;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        Enviroment environment
) {
}
