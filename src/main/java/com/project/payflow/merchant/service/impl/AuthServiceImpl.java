package com.project.payflow.merchant.service.impl;

import com.project.payflow.common.enums.MerchantStatus;
import com.project.payflow.common.enums.UserRole;
import com.project.payflow.common.exception.DuplicateResourceException;
import com.project.payflow.common.exception.ResourceNotFoundException;
import com.project.payflow.merchant.dto.request.LoginRequest;
import com.project.payflow.merchant.dto.request.MerchantSignupRequest;
import com.project.payflow.merchant.dto.response.LoginResponse;
import com.project.payflow.merchant.dto.response.MerchantResponse;
import com.project.payflow.merchant.entity.AppUser;
import com.project.payflow.merchant.entity.Merchant;
import com.project.payflow.merchant.mapper.MerchantMapper;
import com.project.payflow.merchant.repository.AppUserRepository;
import com.project.payflow.merchant.repository.MerchantRepository;
import com.project.payflow.merchant.security.JwtUtil;
import com.project.payflow.merchant.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL",
                    "Merchant with email already exists: "+request.email());
        }

//        Merchant merchant = Merchant.builder()
//                .businessName(request.businessName())
//                .businessType(request.businessType())
//                .name(request.name())
//                .email(request.email())
//                .status(MerchantStatus.PENDING_KYC)
//                .build();

        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(request);
        merchant.setStatus(MerchantStatus.PENDING_KYC);

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);

//        return new MerchantResponse(merchant.getId(), merchant.getName(),
//                merchant.getEmail(), merchant.getBusinessName(),
//                merchant.getBusinessType(), merchant.getStatus());

        return merchantMapper.toResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
       authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(request.email(), request.password())
       );

       AppUser appUser = appUserRepository.findByEmail(request.email())
               .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));

       String token = jwtUtil.generateAccessToken(request.email(), appUser.getMerchant().getId(), appUser.getRole().toString());

       return new LoginResponse(token);

    }
}
