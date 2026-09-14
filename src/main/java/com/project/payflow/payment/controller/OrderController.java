package com.project.payflow.payment.controller;

import com.project.payflow.merchant.security.MerchantContext;
import com.project.payflow.payment.dto.request.CreateOrderRequest;
import com.project.payflow.payment.dto.response.OrderResponse;
import com.project.payflow.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final MerchantContext merchantContext;

    private final OrderService orderService;

    // UUID merchantId = UUID.fromString("");

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(merchantContext.getMerchantId(), request));
    }

}


