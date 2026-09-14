package com.project.payflow.payment.gateway.adapter;

import com.project.payflow.common.enums.PaymentMethod;
import com.project.payflow.payment.gateway.PaymentAdapter;
import com.project.payflow.payment.gateway.dto.PaymentRequest;
import com.project.payflow.payment.gateway.dto.PaymentResult;
import com.project.payflow.payment.processor.PaymentProcessorRouter;
import com.project.payflow.payment.processor.dto.PaymentProcessorRequest;
import com.project.payflow.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request){
        log.info("Initiate Payment with NetBanking, paymentId: {}", request.paymentId());
    try {
        PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                request.paymentId(),
                PaymentMethod.NETBANKING,
                request.amount(),
                request.methodDetails()
        );

        PaymentProcessorResponse paymentProcessorResponse =
                paymentProcessorRouter.charge(paymentProcessorRequest);

        return switch (paymentProcessorResponse) {
            case PaymentProcessorResponse.Failure failure ->
                    new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());

            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());

            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
        };
    }catch (Exception e){
        log.warn("Netbanking failed, paymentId: {}", request.paymentId());
        return new PaymentResult.Failure("NBK_FAILED", e.getMessage());
    }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NBK_REF");
    }
}
