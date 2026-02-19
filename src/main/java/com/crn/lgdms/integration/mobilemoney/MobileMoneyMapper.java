package com.crn.lgdms.integration.mobilemoney;

import com.crn.lgdms.modules.payments.domain.entity.Payment;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class MobileMoneyMapper {

    public MobileMoneyClient.PaymentRequest toPaymentRequest(Payment payment) {
        MobileMoneyClient.PaymentRequest request = new MobileMoneyClient.PaymentRequest();

        request.setPhoneNumber(payment.getCustomer() != null ?
            payment.getCustomer().getPhone() : "");
        request.setAmount(payment.getAmount());
        request.setReference(payment.getPaymentNumber());
        request.setDescription("Payment for LPG products");

        return request;
    }

    public String extractReferenceFromResponse(MobileMoneyClient.PaymentResponse response) {
        return response.getTransactionId();
    }
}
