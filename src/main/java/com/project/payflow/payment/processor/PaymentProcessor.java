package com.project.payflow.payment.processor;

import com.project.payflow.payment.processor.dto.PaymentProcessorRequest;
import com.project.payflow.payment.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);

}
