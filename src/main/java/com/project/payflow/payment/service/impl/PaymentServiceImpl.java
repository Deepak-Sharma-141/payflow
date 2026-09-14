package com.project.payflow.payment.service.impl;

import com.project.payflow.common.enums.OrderStatus;
import com.project.payflow.common.enums.PaymentEvent;
import com.project.payflow.common.enums.PaymentStatus;
import com.project.payflow.common.exception.BusinessRuleViolationException;
import com.project.payflow.common.exception.ResourceNotFoundException;
import com.project.payflow.payment.dto.request.PaymentInitRequestDto;
import com.project.payflow.payment.dto.response.PaymentResponse;
import com.project.payflow.payment.entity.OrderRecord;
import com.project.payflow.payment.entity.Payment;
import com.project.payflow.payment.gateway.PaymentGatewayRouter;
import com.project.payflow.payment.gateway.dto.PaymentRequest;
import com.project.payflow.payment.gateway.dto.PaymentResult;
import com.project.payflow.payment.mapper.PaymentMapper;
import com.project.payflow.payment.repository.OrderRepository;
import com.project.payflow.payment.repository.PaymentRepository;
import com.project.payflow.payment.service.PaymentService;
import com.project.payflow.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private  final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequestDto request) {
//        OrderRecord order = orderRepository.findByIdAndMerchantId(request.orderId(), merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));


        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE",
                    "Order cannot accept payment in status: "+order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment = Payment.builder()
                .order(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(request.method())
                .methodDetails(request.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),
                request.orderId(), merchantId, order.getAmount(),
                request.method(), request.methodDetails());

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult result = paymentGatewayRouter.initiate(paymentRequest);

        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
               // payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success-> {
                log.warn("Invalid state");
                return null;
            }
        }

        payment = paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

//        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));


        //  payment.setStatus(PaymentStatus.CAPTURING); //TODO
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);
        if(paymentResult instanceof PaymentResult.Success success){
            //payment.setStatus(PaymentStatus.CAPTURED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment captured paymentId: {}", paymentId);
        } else if (paymentResult instanceof PaymentResult.Failure failure) {
           // payment.setStatus(PaymentStatus.AUTHORIZED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.info("Payment capture failed, paymentId {}", paymentId);
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef,
                                     Object errorCode, Object errorDescription) {
//        Payment payment = paymentRepository.findById(paymentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));


        if(payment.getStatus() != PaymentStatus.AUTHORIZING){
            log.warn("Payment is not in Authorizing state paymentID: {}, state: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();

        if(approve){
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if(captureResult instanceof PaymentResult.Success success){
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            } else if (captureResult instanceof PaymentResult.Failure failure) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode((String) errorCode);
            payment.setErrorDescription((String) errorDescription);
        }
        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
    }
}

// open for extension
// closed for modification
