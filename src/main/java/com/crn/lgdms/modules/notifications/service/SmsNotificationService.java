package com.crn.lgdms.modules.notifications.service;

import com.crn.lgdms.integration.sms.SmsGatewayClient;
import com.crn.lgdms.integration.sms.SmsMapper;
import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final SmsGatewayClient smsGatewayClient;
    private final SmsMapper smsMapper;

    public void sendRefillReminder(Customer customer) {
        if (customer.getPhone() == null || customer.getPhone().isEmpty()) {
            log.warn("Cannot send SMS to customer {}: no phone number", customer.getId());
            return;
        }

        String message = smsMapper.createRefillReminderMessage(customer);
        smsGatewayClient.sendSms(customer.getPhone(), message);
    }

    public void sendPaymentReminder(CreditAccount account) {
        Customer customer = account.getCustomer();
        if (customer == null || customer.getPhone() == null || customer.getPhone().isEmpty()) {
            log.warn("Cannot send SMS: no customer or phone number");
            return;
        }

        String message = smsMapper.createPaymentReminderMessage(account);
        smsGatewayClient.sendSms(customer.getPhone(), message);
    }

    public void sendLowStockAlert(String locationName, String product, int quantity, String managerPhone) {
        if (managerPhone == null || managerPhone.isEmpty()) {
            log.warn("Cannot send low stock alert: no manager phone");
            return;
        }

        String message = smsMapper.createLowStockAlertMessage(locationName, product, quantity);
        smsGatewayClient.sendSms(managerPhone, message);
    }
}
