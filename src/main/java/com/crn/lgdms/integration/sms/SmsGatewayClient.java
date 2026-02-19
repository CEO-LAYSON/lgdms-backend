package com.crn.lgdms.integration.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SmsGatewayClient {

    @Value("${integration.sms.url:}")
    private String smsUrl;

    @Value("${integration.sms.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSms(String phoneNumber, String message) {
        if (smsUrl.isEmpty()) {
            log.debug("SMS integration not configured, would send: {} to {}", message, phoneNumber);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", phoneNumber);
            payload.put("message", message);
            payload.put("apiKey", apiKey);

            // In real implementation, would POST to SMS gateway
            // restTemplate.postForEntity(smsUrl + "/send", payload, String.class);

            log.info("SMS sent to {}: {}", phoneNumber, message);
        } catch (Exception e) {
            log.error("Failed to send SMS", e);
        }
    }

    public void sendRefillReminder(String phoneNumber, String customerName) {
        String message = String.format(
            "Dear %s, your LPG cylinder is due for refill. Please visit our branch to refill. - LGDMS",
            customerName);

        sendSms(phoneNumber, message);
    }

    public void sendPaymentReminder(String phoneNumber, String customerName, BigDecimal amount) {
        String message = String.format(
            "Dear %s, your payment of %s is overdue. Please clear your balance to continue enjoying our services. - LGDMS",
            customerName, amount);

        sendSms(phoneNumber, message);
    }
}
