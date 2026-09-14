package com.project.payflow.statemachine;

import com.project.payflow.common.enums.PaymentActor;
import com.project.payflow.common.enums.PaymentEvent;
import com.project.payflow.common.enums.PaymentStatus;
import com.project.payflow.payment.entity.Payment;
import com.project.payflow.payment.entity.PaymentTransitionLog;
import com.project.payflow.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent event){
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), event);
        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .event(event)
                .actor(PaymentActor.SYSTEM)
                .occurredAt(LocalDateTime.now())
                .build();
        payment.setStatus(next);
        paymentTransitionLogRepository.save(paymentTransitionLog);
        return next;
    }
}
