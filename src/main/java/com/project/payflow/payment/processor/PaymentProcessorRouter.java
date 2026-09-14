package com.project.payflow.payment.processor;

import com.project.payflow.common.enums.PaymentMethod;
import com.project.payflow.payment.processor.dto.PaymentProcessorRequest;
import com.project.payflow.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessor;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessor.get(request.method());
        if(processor == null){
            throw new IllegalArgumentException("No payment processor registered for method: "+request.method());
        }
        return processor.charge(request);


    }
}
