package com.project.payflow.merchant.cache;

import com.project.payflow.common.enums.Enviroment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(
        String keyId,
        String keySecretHash,
        String previousKeySecretHash,
        LocalDateTime gracePeriodExpiresAt,
        UUID merchantId,
        Enviroment enviroment,
        boolean enabled
) {

    public boolean isInGracePeriod(){
        return gracePeriodExpiresAt != null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }

}
