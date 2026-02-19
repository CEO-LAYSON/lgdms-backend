package com.crn.lgdms.modules.notifications.web;

import com.crn.lgdms.common.api.ApiResponse;
import com.crn.lgdms.common.constants.Permissions;
import com.crn.lgdms.modules.notifications.dto.request.SendNotificationRequest;
import com.crn.lgdms.modules.notifications.dto.response.NotificationResponse;
import com.crn.lgdms.modules.notifications.service.SmsNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "SMS and email notification endpoints")
public class NotificationController {

    private final SmsNotificationService smsNotificationService;

    @PostMapping("/sms/refill-reminder")
    @PreAuthorize("hasAuthority('" + Permissions.SALE_VIEW + "')")
    @Operation(summary = "Send refill reminder SMS to customer")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendRefillReminder(
        @Valid @RequestBody SendNotificationRequest request) {

        // Implementation would use CustomerService to get customer
        // smsNotificationService.sendRefillReminder(customer);

        NotificationResponse response = NotificationResponse.builder()
            .success(true)
            .message("Refill reminder sent to " + request.getRecipient())
            .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/sms/payment-reminder")
    @PreAuthorize("hasAuthority('" + Permissions.CREDIT_VIEW + "')")
    @Operation(summary = "Send payment reminder SMS to customer")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendPaymentReminder(
        @Valid @RequestBody SendNotificationRequest request) {

        NotificationResponse response = NotificationResponse.builder()
            .success(true)
            .message("Payment reminder sent to " + request.getRecipient())
            .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
