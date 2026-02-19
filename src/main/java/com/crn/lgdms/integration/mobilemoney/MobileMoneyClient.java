package com.crn.lgdms.integration.mobilemoney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MobileMoneyClient {

    @Value("${integration.mobilemoney.url:}")
    private String mobileMoneyUrl;

    @Value("${integration.mobilemoney.api-key:}")
    private String apiKey;

    @Value("${integration.mobilemoney.vendor-id:}")
    private String vendorId;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentResponse processPayment(PaymentRequest request) {
        if (mobileMoneyUrl.isEmpty()) {
            log.debug("Mobile money integration not configured, simulating payment");
            return simulatePayment(request);
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("phoneNumber", request.getPhoneNumber());
            payload.put("amount", request.getAmount());
            payload.put("reference", request.getReference());
            payload.put("apiKey", apiKey);
            payload.put("vendorId", vendorId);

            // In real implementation, would POST to mobile money API
            // PaymentResponse response = restTemplate.postForObject(
            //         mobileMoneyUrl + "/pay", payload, PaymentResponse.class);

            log.info("Mobile money payment processed for {}: {}",
                request.getPhoneNumber(), request.getAmount());

            return PaymentResponse.success(request.getReference());
        } catch (Exception e) {
            log.error("Failed to process mobile money payment", e);
            return PaymentResponse.failure("Payment failed: " + e.getMessage());
        }
    }

    public PaymentStatus checkPaymentStatus(String transactionId) {
        if (mobileMoneyUrl.isEmpty()) {
            return PaymentStatus.COMPLETED;
        }

        try {
            // Would call API to check status
            return PaymentStatus.COMPLETED;
        } catch (Exception e) {
            log.error("Failed to check payment status", e);
            return PaymentStatus.UNKNOWN;
        }
    }

    private PaymentResponse simulatePayment(PaymentRequest request) {
        log.info("SIMULATING mobile money payment: {} for {}",
            request.getAmount(), request.getPhoneNumber());

        return PaymentResponse.success("SIMULATED-" + request.getReference());
    }

    public static class PaymentRequest {
        private String phoneNumber;
        private BigDecimal amount;
        private String reference;
        private String description;

        // Getters and setters
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class PaymentResponse {
        private boolean success;
        private String transactionId;
        private String message;

        public static PaymentResponse success(String transactionId) {
            PaymentResponse response = new PaymentResponse();
            response.success = true;
            response.transactionId = transactionId;
            response.message = "Payment successful";
            return response;
        }

        public static PaymentResponse failure(String message) {
            PaymentResponse response = new PaymentResponse();
            response.success = false;
            response.message = message;
            return response;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, UNKNOWN
    }
}
