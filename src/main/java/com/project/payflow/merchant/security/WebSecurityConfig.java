package com.project.payflow.merchant.security;

import com.project.payflow.common.idempotency.IdempotencyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final String[] JWT_ROUTES = {"/v1/auth/**", "/v1/merchants/**", "/v1/admin/**", "/actuator/**"};
    private static final String[] API_KEY_ROUTES = {"/v1/orders/**", "/v1/payments/**", "/v1/vault/**"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final IdempotencyFilter idempotencyFilter;

    @Bean
    public SecurityFilterChain jwtChain(HttpSecurity httpSecurity){
        return httpSecurity
                .securityMatcher(JWT_ROUTES)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/signup", "/v1/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(idempotencyFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Bean
    public SecurityFilterChain apiKeyChain(HttpSecurity httpSecurity){
        return httpSecurity
                .securityMatcher(API_KEY_ROUTES)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                ))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(idempotencyFilter, ApiKeyAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(MerchantUserDetailService userDetailService,
                                                       PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);

    }
}
