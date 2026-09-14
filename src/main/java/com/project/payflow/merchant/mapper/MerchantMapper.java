package com.project.payflow.merchant.mapper;

import com.project.payflow.merchant.dto.request.MerchantSignupRequest;
import com.project.payflow.merchant.dto.response.MerchantResponse;
import com.project.payflow.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toEntityFromSignUpRequest(MerchantSignupRequest request);

    MerchantResponse toResponse(Merchant merchant);
}
