package com.crn.lgdms.modules.payments.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.PaymentMethod;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.common.exception.ValidationException;
import com.crn.lgdms.modules.credit.service.CreditService;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.payments.domain.entity.CashbookEntry;
import com.crn.lgdms.modules.payments.domain.entity.Payment;
import com.crn.lgdms.modules.payments.domain.entity.PaymentAllocation;
import com.crn.lgdms.modules.payments.dto.request.RecordPaymentRequest;
import com.crn.lgdms.modules.payments.dto.response.PaymentResponse;
import com.crn.lgdms.modules.payments.dto.mapper.PaymentMapper;
import com.crn.lgdms.modules.payments.repository.CashbookEntryRepository;
import com.crn.lgdms.modules.payments.repository.PaymentAllocationRepository;
import com.crn.lgdms.modules.payments.repository.PaymentRepository;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.sales.domain.entity.Sale;
import com.crn.lgdms.modules.sales.repository.CustomerRepository;
import com.crn.lgdms.modules.sales.repository.SaleRepository;
import com.crn.lgdms.modules.users.domain.entity.User;
import com.crn.lgdms.modules.users.repository.UserRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CashbookEntryRepository cashbookEntryRepository;
    private final LocationRepository locationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final CreditService creditService;

    private final PaymentMapper paymentMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public PaymentResponse recordPayment(RecordPaymentRequest request) {
        log.info("Recording payment of {} at location: {}", request.getAmount(), request.getLocationId());

        // Validate location
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + request.getLocationId()));

        // Validate received by user
        User receivedBy = userRepository.findById(request.getReceivedBy())
            .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getReceivedBy()));

        // Validate customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + request.getCustomerId()));
        }

        // Create payment
        Payment payment = Payment.builder()
            .paymentNumber(generatePaymentNumber())
            .location(location)
            .customer(customer)
            .receivedBy(receivedBy)
            .paymentDate(request.getPaymentDate())
            .paymentMethod(request.getPaymentMethod())
            .amount(request.getAmount())
            .referenceNumber(request.getReferenceNumber())
            .bankName(request.getBankName())
            .chequeDate(request.getChequeDate())
            .status(TransactionStatus.PENDING)
            .notes(request.getNotes())
            .allocations(new ArrayList<>())
            .build();

        // Process allocations if provided
        BigDecimal totalAllocated = BigDecimal.ZERO;
        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            for (RecordPaymentRequest.AllocationRequest allocRequest : request.getAllocations()) {
                Sale sale = saleRepository.findById(allocRequest.getSaleId())
                    .orElseThrow(() -> new NotFoundException("Sale not found with id: " + allocRequest.getSaleId()));

                // Validate that sale belongs to same customer
                if (customer != null && (sale.getCustomer() == null ||
                    !sale.getCustomer().getId().equals(customer.getId()))) {
                    throw new ValidationException("Sale does not belong to the specified customer");
                }

                // Validate allocation amount doesn't exceed sale balance
                BigDecimal alreadyAllocated = paymentAllocationRepository.getTotalAllocatedToSale(sale.getId());
                if (alreadyAllocated == null) alreadyAllocated = BigDecimal.ZERO;

                BigDecimal saleBalance = sale.getTotalAmount().subtract(sale.getPaidAmount()).subtract(alreadyAllocated);

                if (allocRequest.getAllocatedAmount().compareTo(saleBalance) > 0) {
                    throw new ValidationException(
                        String.format("Allocation amount %s exceeds sale balance %s",
                            allocRequest.getAllocatedAmount(), saleBalance)
                    );
                }

                PaymentAllocation allocation = PaymentAllocation.builder()
                    .payment(payment)
                    .sale(sale)
                    .allocatedAmount(allocRequest.getAllocatedAmount())
                    .allocationDate(LocalDateTime.now())
                    .notes("Allocated from payment")
                    .build();

                payment.getAllocations().add(allocation);
                totalAllocated = totalAllocated.add(allocRequest.getAllocatedAmount());

                // Update sale paid amount
                sale.setPaidAmount(sale.getPaidAmount().add(allocRequest.getAllocatedAmount()));
                sale.setBalanceDue(sale.getTotalAmount().subtract(sale.getPaidAmount()));
                saleRepository.save(sale);

                // If this is a credit sale, record payment against credit
                if (sale.isCreditSale()) {
                    creditService.recordPaymentAgainstCredit(
                        creditService.getCreditAccountByCustomerId(sale.getCustomer().getId()).getId(),
                        allocRequest.getAllocatedAmount(),
                        payment.getPaymentNumber()
                    );
                }
            }

            // Validate total allocation doesn't exceed payment amount
            if (totalAllocated.compareTo(request.getAmount()) > 0) {
                throw new ValidationException("Total allocated amount exceeds payment amount");
            }

            // If fully allocated, mark payment as COMPLETED
            if (totalAllocated.compareTo(request.getAmount()) == 0) {
                payment.setStatus(TransactionStatus.COMPLETED);
            }
        } else {
            // No allocations - payment is unallocated (will be in suspense)
            payment.setStatus(TransactionStatus.PENDING);
        }

        // Create cashbook entry
        CashbookEntry cashbookEntry = CashbookEntry.builder()
            .entryNumber(generateCashbookEntryNumber())
            .location(location)
            .entryDate(LocalDateTime.now())
            .entryType(CashbookEntry.EntryType.RECEIPT)
            .paymentMethod(request.getPaymentMethod())
            .amount(request.getAmount())
            .referenceType("PAYMENT")
            .referenceId(payment.getId())
            .referenceNumber(payment.getPaymentNumber())
            .description("Payment received from: " + (customer != null ? customer.getName() : "Unknown"))
            .createdBy(receivedBy.getUsername())
            .build();

        cashbookEntryRepository.save(cashbookEntry);

        Payment saved = paymentRepository.save(payment);

        auditLogService.log(AuditAction.CREATE, "Payment", saved.getId(),
            null, saved.getPaymentNumber(), receivedBy.getUsername());

        log.info("Payment recorded with number: {}", saved.getPaymentNumber());
        return paymentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "payments", key = "#id")
    public PaymentResponse allocatePayment(String paymentId, List<RecordPaymentRequest.AllocationRequest> allocations) {
        log.info("Allocating payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == TransactionStatus.COMPLETED) {
            throw new ConflictException("Payment already fully allocated");
        }

        BigDecimal totalAllocated = payment.getAllocations().stream()
            .map(PaymentAllocation::getAllocatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (RecordPaymentRequest.AllocationRequest allocRequest : allocations) {
            Sale sale = saleRepository.findById(allocRequest.getSaleId())
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + allocRequest.getSaleId()));

            // Validate sale belongs to same customer
            if (payment.getCustomer() != null && (sale.getCustomer() == null ||
                !sale.getCustomer().getId().equals(payment.getCustomer().getId()))) {
                throw new ValidationException("Sale does not belong to the payment customer");
            }

            // Validate allocation amount
            BigDecimal alreadyAllocatedToSale = paymentAllocationRepository.getTotalAllocatedToSale(sale.getId());
            if (alreadyAllocatedToSale == null) alreadyAllocatedToSale = BigDecimal.ZERO;

            BigDecimal saleBalance = sale.getTotalAmount().subtract(sale.getPaidAmount());

            if (allocRequest.getAllocatedAmount().compareTo(saleBalance) > 0) {
                throw new ValidationException(
                    String.format("Allocation amount %s exceeds sale balance %s",
                        allocRequest.getAllocatedAmount(), saleBalance)
                );
            }

            // Check if this would exceed total payment amount
            if (totalAllocated.add(allocRequest.getAllocatedAmount()).compareTo(payment.getAmount()) > 0) {
                throw new ValidationException("Total allocation would exceed payment amount");
            }

            PaymentAllocation allocation = PaymentAllocation.builder()
                .payment(payment)
                .sale(sale)
                .allocatedAmount(allocRequest.getAllocatedAmount())
                .allocationDate(LocalDateTime.now())
                .notes("Allocated from payment")
                .build();

            paymentAllocationRepository.save(allocation);
            payment.getAllocations().add(allocation);

            // Update sale paid amount
            sale.setPaidAmount(sale.getPaidAmount().add(allocRequest.getAllocatedAmount()));
            sale.setBalanceDue(sale.getTotalAmount().subtract(sale.getPaidAmount()));
            saleRepository.save(sale);

            totalAllocated = totalAllocated.add(allocRequest.getAllocatedAmount());

            // If this is a credit sale, record payment against credit
            if (sale.isCreditSale()) {
                creditService.recordPaymentAgainstCredit(
                    creditService.getCreditAccountByCustomerId(sale.getCustomer().getId()).getId(),
                    allocRequest.getAllocatedAmount(),
                    payment.getPaymentNumber()
                );
            }
        }

        // If fully allocated now, update status
        if (totalAllocated.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(TransactionStatus.COMPLETED);
        }

        Payment updated = paymentRepository.save(payment);

        auditLogService.log(AuditAction.UPDATE, "Payment", paymentId,
            null, "Allocated: " + totalAllocated, getCurrentUsername());

        return paymentMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "payments", key = "#id")
    public PaymentResponse getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
        return enhancePaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
            .orElseThrow(() -> new NotFoundException("Payment not found with number: " + paymentNumber));
        return enhancePaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable)
            .map(this::enhancePaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByLocation(String locationId, Pageable pageable) {
        return paymentRepository.findByLocationId(locationId, pageable)
            .map(this::enhancePaymentResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByCustomer(String customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId).stream()
            .map(this::enhancePaymentResponse)
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
    }

    @Transactional
    @CacheEvict(value = "payments", key = "#id")
    public void voidPayment(String id, String reason) {
        log.info("Voiding payment: {}", id);

        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() == TransactionStatus.VOID) {
            throw new ConflictException("Payment already voided");
        }

        // Reverse allocations
        for (PaymentAllocation allocation : payment.getAllocations()) {
            Sale sale = allocation.getSale();
            sale.setPaidAmount(sale.getPaidAmount().subtract(allocation.getAllocatedAmount()));
            sale.setBalanceDue(sale.getBalanceDue().add(allocation.getAllocatedAmount()));
            saleRepository.save(sale);

            // Reverse credit if applicable
            if (sale.isCreditSale()) {
                // Record negative payment against credit (reversal)
                creditService.recordPaymentAgainstCredit(
                    creditService.getCreditAccountByCustomerId(sale.getCustomer().getId()).getId(),
                    allocation.getAllocatedAmount().negate(),
                    "VOID:" + payment.getPaymentNumber()
                );
            }
        }

        // Create reversal cashbook entry
        CashbookEntry reversal = CashbookEntry.builder()
            .entryNumber(generateCashbookEntryNumber())
            .location(payment.getLocation())
            .entryDate(LocalDateTime.now())
            .entryType(CashbookEntry.EntryType.PAYMENT) // Negative entry
            .paymentMethod(payment.getPaymentMethod())
            .amount(payment.getAmount().negate())
            .referenceType("PAYMENT_VOID")
            .referenceId(payment.getId())
            .referenceNumber(payment.getPaymentNumber())
            .description("Payment voided: " + reason)
            .createdBy(getCurrentUsername())
            .build();

        cashbookEntryRepository.save(reversal);

        payment.setStatus(TransactionStatus.VOID);
        payment.setNotes(payment.getNotes() + " | VOIDED: " + reason);
        paymentRepository.save(payment);

        auditLogService.log(AuditAction.DELETE, "Payment", id,
            payment.getPaymentNumber(), "VOIDED", getCurrentUsername());
    }

    private PaymentResponse enhancePaymentResponse(Payment payment) {
        PaymentResponse response = paymentMapper.toResponse(payment);

        BigDecimal allocated = payment.getAllocations().stream()
            .map(PaymentAllocation::getAllocatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setAllocatedAmount(allocated);
        response.setUnallocatedAmount(payment.getAmount().subtract(allocated));

        return response;
    }

    private String generatePaymentNumber() {
        String prefix = "PAY";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%06d", getNextPaymentSequence());
        return prefix + "-" + date + "-" + sequence;
    }

    private String generateCashbookEntryNumber() {
        String prefix = "CB";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%06d", getNextCashbookSequence());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextPaymentSequence() {
        return (int) (paymentRepository.count() + 1);
    }

    private synchronized int getNextCashbookSequence() {
        return (int) (cashbookEntryRepository.count() + 1);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
