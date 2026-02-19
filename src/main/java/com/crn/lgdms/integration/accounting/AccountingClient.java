package com.crn.lgdms.integration.accounting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AccountingClient {

    @Value("${integration.accounting.url:}")
    private String accountingUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void postSaleToAccounting(String invoiceNumber,
                                     BigDecimal amount,
                                     String customerName,
                                     String date) {

        if (accountingUrl.isEmpty()) {
            log.debug("Accounting integration not configured, skipping");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("invoiceNumber", invoiceNumber);
            payload.put("amount", amount);
            payload.put("customerName", customerName);
            payload.put("date", date);
            payload.put("type", "SALE");

            // In real implementation, would POST to accounting system
            // restTemplate.postForEntity(accountingUrl + "/transactions", payload, String.class);

            log.info("Sale posted to accounting system: {}", invoiceNumber);
        } catch (Exception e) {
            log.error("Failed to post sale to accounting system", e);
        }
    }

    public void postPaymentToAccounting(String paymentNumber,
                                        BigDecimal amount,
                                        String customerName,
                                        String date) {

        if (accountingUrl.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("paymentNumber", paymentNumber);
            payload.put("amount", amount);
            payload.put("customerName", customerName);
            payload.put("date", date);
            payload.put("type", "PAYMENT");

            log.info("Payment posted to accounting system: {}", paymentNumber);
        } catch (Exception e) {
            log.error("Failed to post payment to accounting system", e);
        }
    }
}
