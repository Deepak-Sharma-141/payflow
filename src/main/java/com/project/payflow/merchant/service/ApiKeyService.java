package com.project.payflow.merchant.service;


import com.project.payflow.merchant.dto.request.CreateApiKeyRequest;
import com.project.payflow.merchant.dto.response.ApiKeyCreateResponse;
import com.project.payflow.merchant.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
