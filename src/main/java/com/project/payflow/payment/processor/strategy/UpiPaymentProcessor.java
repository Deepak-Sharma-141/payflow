package com.project.payflow.payment.processor.strategy;

import com.deepakproject.razorpay.common.util.RandomizerUtil;
import com.deepakproject.razorpay.payment.processor.PaymentProcessor;
import com.deepakproject.razorpay.payment.processor.dto.PaymentProcessorRequest;
import com.deepakproject.razorpay.payment.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "failed@okaxis";

        String bankCode = request.methodDetails()!= null ?
                request.methodDetails().get("vpa").toString() : null;

        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "bank rejected the transaction registration");
        }

        String processorRef = "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        String bankRef = "BANK_REF"+RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Success(processorRef, bankRef);
    }
}
