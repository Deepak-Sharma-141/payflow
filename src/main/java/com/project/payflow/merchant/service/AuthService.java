package com.project.payflow.merchant.service;

import com.project.payflow.merchant.dto.request.LoginRequest;
import com.project.payflow.merchant.dto.request.MerchantSignupRequest;
import com.project.payflow.merchant.dto.response.LoginResponse;
import com.project.payflow.merchant.dto.response.MerchantResponse;

public interface AuthService {
    MerchantResponse signup(MerchantSignupRequest request);

    LoginResponse login(LoginRequest request);
}
