package com.crn.lgdms.integration.sms;

import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class SmsMapper {

    public String createRefillReminderMessage(Customer customer) {
        return String.format(
            "Hello %s,\n\nYour LPG cylinder refill is ready. Please visit our branch to collect.\n\nThank you for choosing LGDMS.",
            customer.getName());
    }

    public String createPaymentReminderMessage(CreditAccount account) {
        String customerName = account.getCustomer() != null ?
            account.getCustomer().getName() : "Customer";

        return String.format(
            "Dear %s,\n\nYour outstanding balance is %s. Please make payment to avoid service interruption.\n\nLGDMS",
            customerName, account.getCurrentBalance());
    }

    public String createLowStockAlertMessage(String locationName, String product, int quantity) {
        return String.format(
            "ALERT: Low stock at %s. %s has only %d units remaining. Please restock.",
            locationName, product, quantity);
    }
}
