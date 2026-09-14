package com.project.payflow.merchant.service.impl;

import com.project.payflow.common.exception.ResourceNotFoundException;
import com.project.payflow.common.util.RandomizerUtil;
import com.project.payflow.merchant.cache.ApiKeyCache;
import com.project.payflow.merchant.dto.request.CreateApiKeyRequest;
import com.project.payflow.merchant.dto.response.ApiKeyCreateResponse;
import com.project.payflow.merchant.dto.response.ApiKeyResponse;
import com.project.payflow.merchant.entity.ApiKey;
import com.project.payflow.merchant.entity.Merchant;
import com.project.payflow.merchant.mapper.ApiKeyMapper;
import com.project.payflow.merchant.repository.ApiKeyRepository;
import com.project.payflow.merchant.repository.MerchantRepository;
import com.project.payflow.merchant.service.ApiKeyService;
import jakarta.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final BCryptPasswordEncoder  BCRYPT = new BCryptPasswordEncoder();
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "rzp_"+request.enviroment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(BCRYPT.encode(rawSecret))
                .environment(request.enviroment())
                .build();

        apiKey = apiKeyRepository.save(apiKey);


        return new ApiKeyCreateResponse(apiKey.getId(), keyId, rawSecret, request.enviroment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
//        return apiKeyRepository.findByMerchant_Id(merchantId).stream()
//                .map(apiKey -> new ApiKeyResponse(
//                        apiKey.getId(),
//                        apiKey.getKeyId(),
//                        apiKey.getEnvironment(),
//                        apiKey.isEnabled(),
//                        apiKey.getLastUsedAt(), null))
//                .toList();

        return apiKeyMapper.toResponseList(apiKeyRepository.findByMerchant_Id(merchantId));
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));

        key.setEnabled(false);
        apiKeyCache.evict(key.getKeyId());
        apiKeyRepository.save(key);
    }

    @Override
    @Transactional
    public  @Nullable ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId) {

        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));

        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot rotate a disabled key");

        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret)); //TODO: encode with BcryptPasswordEncoder
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        apiKey = apiKeyRepository.save(apiKey);

        apiKeyCache.evict(apiKey.getKeyId());

        return new ApiKeyCreateResponse(apiKey.getId(), apiKey.getKeyId(),
                newRawSecret, apiKey.getEnvironment());
    }
}
