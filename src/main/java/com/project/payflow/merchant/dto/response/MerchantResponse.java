package com.project.payflow.merchant.dto.response;

import com.project.payflow.common.enums.BusinessType;
import com.project.payflow.common.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(

        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus

) {
}
