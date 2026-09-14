package com.project.payflow.payment.config;

import com.project.payflow.common.enums.PaymentMethod;
import com.project.payflow.payment.processor.PaymentProcessor;
import com.project.payflow.payment.processor.strategy.CardPaymentProcessor;
import com.project.payflow.payment.processor.strategy.NetBankingPaymentProcessor;
import com.project.payflow.payment.processor.strategy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod , PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
