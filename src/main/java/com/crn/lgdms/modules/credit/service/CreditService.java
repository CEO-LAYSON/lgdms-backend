package com.crn.lgdms.modules.credit.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.common.exception.ValidationException;
import com.crn.lgdms.modules.credit.domain.entity.*;
import com.crn.lgdms.modules.credit.dto.request.SetCreditLimitRequest;
import com.crn.lgdms.modules.credit.dto.response.CreditAccountResponse;
import com.crn.lgdms.modules.credit.dto.response.CreditAgingResponse;
import com.crn.lgdms.modules.credit.dto.response.CreditTransactionResponse;
import com.crn.lgdms.modules.credit.dto.mapper.CreditAccountMapper;
import com.crn.lgdms.modules.credit.dto.mapper.CreditMapper;
import com.crn.lgdms.modules.credit.repository.CreditAccountRepository;
import com.crn.lgdms.modules.credit.repository.CreditLimitRepository;
import com.crn.lgdms.modules.credit.repository.CreditTransactionRepository;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.sales.domain.entity.Sale;
import com.crn.lgdms.modules.sales.repository.CustomerRepository;
import com.crn.lgdms.modules.sales.repository.SaleRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditAccountRepository creditAccountRepository;
    private final CreditLimitRepository creditLimitRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final CustomerRepository customerRepository;
    private final LocationRepository locationRepository;
    private final SaleRepository saleRepository;

    private final CreditAccountMapper creditAccountMapper;
    private final CreditMapper creditMapper;
    private final AuditLogService auditLogService;

    // ================ CREDIT ACCOUNT MANAGEMENT ================

    @Transactional
    public CreditAccountResponse createCreditAccountForCustomer(String customerId) {
        log.info("Creating credit account for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));

        if (creditAccountRepository.findByCustomerId(customerId).isPresent()) {
            throw new ConflictException("Credit account already exists for this customer");
        }

        CreditAccount account = CreditAccount.builder()
            .accountNumber(generateAccountNumber("CUST"))
            .customer(customer)
            .accountType(CreditAccount.CreditAccountType.CUSTOMER)
            .creditLimit(BigDecimal.ZERO)
            .currentBalance(BigDecimal.ZERO)
            .availableCredit(BigDecimal.ZERO)
            .paymentTerms("NET30")
            .isActive(true)
            .build();

        CreditAccount saved = creditAccountRepository.save(account);

        auditLogService.log(AuditAction.CREATE, "CreditAccount", saved.getId(),
            null, saved.getAccountNumber(), getCurrentUsername());

        return creditAccountMapper.toResponse(saved);
    }

    @Transactional
    public CreditAccountResponse createCreditAccountForVehicle(String locationId) {
        log.info("Creating credit account for vehicle: {}", locationId);

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + locationId));

        if (location.getLocationType() != com.crn.lgdms.common.enums.LocationType.VEHICLE) {
            throw new ValidationException("Location must be a VEHICLE type");
        }

        if (creditAccountRepository.findByLocationId(locationId).isPresent()) {
            throw new ConflictException("Credit account already exists for this vehicle");
        }

        CreditAccount account = CreditAccount.builder()
            .accountNumber(generateAccountNumber("VEH"))
            .location(location)
            .accountType(CreditAccount.CreditAccountType.VEHICLE)
            .creditLimit(BigDecimal.ZERO)
            .currentBalance(BigDecimal.ZERO)
            .availableCredit(BigDecimal.ZERO)
            .paymentTerms("NET15")
            .isActive(true)
            .build();

        CreditAccount saved = creditAccountRepository.save(account);

        auditLogService.log(AuditAction.CREATE, "CreditAccount", saved.getId(),
            null, saved.getAccountNumber(), getCurrentUsername());

        return creditAccountMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "creditAccounts", key = "#id")
    public CreditAccountResponse getCreditAccountById(String id) {
        CreditAccount account = creditAccountRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Credit account not found with id: " + id));
        return creditAccountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public CreditAccountResponse getCreditAccountByCustomerId(String customerId) {
        CreditAccount account = creditAccountRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new NotFoundException("Credit account not found for customer: " + customerId));
        return creditAccountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public CreditAccountResponse getCreditAccountByVehicleId(String locationId) {
        CreditAccount account = creditAccountRepository.findByLocationId(locationId)
            .orElseThrow(() -> new NotFoundException("Credit account not found for vehicle: " + locationId));
        return creditAccountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<CreditAccountResponse> getAllCreditAccounts(Pageable pageable) {
        return creditAccountRepository.findAll(pageable)
            .map(creditAccountMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CreditAccountResponse> searchCreditAccounts(String searchTerm, Pageable pageable) {
        return creditAccountRepository.searchCreditAccounts(searchTerm, pageable)
            .map(creditAccountMapper::toResponse);
    }

    // ================ CREDIT LIMIT MANAGEMENT ================

    @Transactional
    public CreditAccountResponse setCreditLimit(SetCreditLimitRequest request) {
        log.info("Setting credit limit for account");

        CreditAccount account;
        if (request.getCustomerId() != null) {
            account = creditAccountRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Credit account not found for customer: " + request.getCustomerId()));
        } else if (request.getLocationId() != null) {
            account = creditAccountRepository.findByLocationId(request.getLocationId())
                .orElseThrow(() -> new NotFoundException("Credit account not found for location: " + request.getLocationId()));
        } else {
            throw new ValidationException("Either customerId or locationId must be provided");
        }

        // Deactivate current limit
        creditLimitRepository.findByCustomerIdAndIsCurrentTrue(account.getCustomer() != null ? account.getCustomer().getId() : null)
            .ifPresent(limit -> {
                limit.setCurrent(false);
                limit.setEffectiveTo(LocalDateTime.now());
                creditLimitRepository.save(limit);
            });

        creditLimitRepository.findByLocationIdAndIsCurrentTrue(account.getLocation() != null ? account.getLocation().getId() : null)
            .ifPresent(limit -> {
                limit.setCurrent(false);
                limit.setEffectiveTo(LocalDateTime.now());
                creditLimitRepository.save(limit);
            });

        // Create new limit
        CreditLimit limit = CreditLimit.builder()
            .customer(account.getCustomer())
            .location(account.getLocation())
            .limitAmount(request.getLimitAmount())
            .effectiveFrom(request.getEffectiveFrom())
            .effectiveTo(request.getEffectiveTo())
            .approvedBy(request.getApprovedBy())
            .approvedAt(LocalDateTime.now())
            .reason(request.getReason())
            .isCurrent(true)
            .build();

        creditLimitRepository.save(limit);

        // Update account credit limit
        account.setCreditLimit(request.getLimitAmount());
        account.calculateAvailableCredit();

        CreditAccount updated = creditAccountRepository.save(account);

        auditLogService.log(AuditAction.UPDATE, "CreditLimit", updated.getId(),
            null, "Limit set to: " + request.getLimitAmount(), request.getApprovedBy());

        return creditAccountMapper.toResponse(updated);
    }

    // ================ CREDIT TRANSACTIONS ================

    @Transactional
    public CreditTransaction recordCreditSale(Sale sale) {
        log.info("Recording credit sale: {}", sale.getInvoiceNumber());

        CreditAccount account;
        if (sale.getCustomer() != null) {
            account = creditAccountRepository.findByCustomerId(sale.getCustomer().getId())
                .orElseThrow(() -> new NotFoundException("Credit account not found for customer"));
        } else {
            throw new ValidationException("Credit sale requires a customer");
        }

        // Check credit limit
        if (account.getCurrentBalance().add(sale.getTotalAmount()).compareTo(account.getCreditLimit()) > 0) {
            throw new ValidationException(
                String.format("Credit limit exceeded. Limit: %s, New balance: %s",
                    account.getCreditLimit(),
                    account.getCurrentBalance().add(sale.getTotalAmount()))
            );
        }

        // Create transaction
        CreditTransaction transaction = CreditTransaction.builder()
            .transactionNumber(generateTransactionNumber())
            .creditAccount(account)
            .sale(sale)
            .transactionType(CreditTransaction.TransactionType.SALE)
            .amount(sale.getTotalAmount())
            .balanceAfter(account.getCurrentBalance().add(sale.getTotalAmount()))
            .description("Credit sale: " + sale.getInvoiceNumber())
            .transactionDate(LocalDateTime.now())
            .status(TransactionStatus.COMPLETED)
            .referenceNumber(sale.getInvoiceNumber())
            .build();

        CreditTransaction saved = creditTransactionRepository.save(transaction);

        // Update account balance
        account.setCurrentBalance(account.getCurrentBalance().add(sale.getTotalAmount()));
        account.calculateAvailableCredit();
        creditAccountRepository.save(account);

        return saved;
    }

    @Transactional
    public CreditTransaction recordPaymentAgainstCredit(String creditAccountId,
                                                        BigDecimal amount,
                                                        String paymentNumber) {
        log.info("Recording payment against credit account: {}", creditAccountId);

        CreditAccount account = creditAccountRepository.findById(creditAccountId)
            .orElseThrow(() -> new NotFoundException("Credit account not found with id: " + creditAccountId));

        CreditTransaction transaction = CreditTransaction.builder()
            .transactionNumber(generateTransactionNumber())
            .creditAccount(account)
            .transactionType(CreditTransaction.TransactionType.PAYMENT)
            .amount(amount.negate()) // Negative for payment
            .balanceAfter(account.getCurrentBalance().subtract(amount))
            .description("Payment received: " + paymentNumber)
            .transactionDate(LocalDateTime.now())
            .status(TransactionStatus.COMPLETED)
            .referenceNumber(paymentNumber)
            .build();

        CreditTransaction saved = creditTransactionRepository.save(transaction);

        // Update account balance
        account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        account.calculateAvailableCredit();
        creditAccountRepository.save(account);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<CreditTransactionResponse> getCreditAccountTransactions(String accountId, Pageable pageable) {
        return creditTransactionRepository.findByCreditAccountId(accountId, pageable)
            .map(creditMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CreditTransactionResponse> getTransactionsByDateRange(String accountId,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate) {
        return creditTransactionRepository.findTransactionsByDateRange(accountId, startDate, endDate)
            .stream()
            .map(creditMapper::toResponse)
            .collect(Collectors.toList());
    }

    // ================ CREDIT AGING ================

    @Transactional(readOnly = true)
    public CreditAgingResponse getAgingReport(LocalDate asOfDate) {
        log.info("Generating credit aging report as of: {}", asOfDate);

        List<CreditAccount> accounts = creditAccountRepository.findAccountsWithBalance(Pageable.unpaged()).getContent();

        List<CreditAgingResponse.AgingBucketResponse> buckets = accounts.stream()
            .map(account -> {
                List<CreditTransaction> transactions = creditTransactionRepository
                    .findByCreditAccountId(account.getId());

                AgingBucket bucket = AgingBucket.createFromTransactions(
                    account.getId(),
                    account.getCustomer() != null ? account.getCustomer().getName() : null,
                    account.getLocation() != null ? account.getLocation().getName() : null,
                    transactions,
                    asOfDate
                );

                return CreditAgingResponse.AgingBucketResponse.builder()
                    .accountId(bucket.getAccountId())
                    .customerName(bucket.getCustomerName())
                    .locationName(bucket.getLocationName())
                    .current(bucket.getCurrent())
                    .days1to30(bucket.getDays1to30())
                    .days31to60(bucket.getDays31to60())
                    .days61to90(bucket.getDays61to90())
                    .over90(bucket.getOver90())
                    .total(bucket.getTotal())
                    .build();
            })
            .filter(bucket -> bucket.getTotal().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());

        // Calculate summary
        CreditAgingResponse.Summary summary = CreditAgingResponse.Summary.builder()
            .totalCurrent(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getCurrent)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .total1to30(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getDays1to30)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .total31to60(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getDays31to60)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .total61to90(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getDays61to90)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .totalOver90(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getOver90)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .grandTotal(buckets.stream().map(CreditAgingResponse.AgingBucketResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .build();

        return CreditAgingResponse.builder()
            .buckets(buckets)
            .summary(summary)
            .build();
    }

    @Transactional(readOnly = true)
    public List<CreditAccountResponse> getAccountsOverLimit() {
        return creditAccountRepository.findAccountsOverLimit().stream()
            .map(creditAccountMapper::toResponse)
            .collect(Collectors.toList());
    }

    // ================ CREDIT BLOCKING ================

    @Transactional(readOnly = true)
    public boolean canCreateCreditSale(String customerId, BigDecimal amount) {
        CreditAccount account = creditAccountRepository.findByCustomerId(customerId)
            .orElse(null);

        if (account == null || !account.isActive()) {
            return false;
        }

        return account.getCurrentBalance().add(amount).compareTo(account.getCreditLimit()) <= 0;
    }

    @Transactional
    public void blockAccount(String accountId, String reason) {
        CreditAccount account = creditAccountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Credit account not found with id: " + accountId));

        account.setActive(false);
        account.setNotes(account.getNotes() + " | BLOCKED: " + reason);
        creditAccountRepository.save(account);

        auditLogService.log(AuditAction.UPDATE, "CreditAccount", accountId,
            "ACTIVE", "BLOCKED", getCurrentUsername());
    }

    @Transactional
    public void unblockAccount(String accountId) {
        CreditAccount account = creditAccountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Credit account not found with id: " + accountId));

        account.setActive(true);
        creditAccountRepository.save(account);

        auditLogService.log(AuditAction.UPDATE, "CreditAccount", accountId,
            "BLOCKED", "ACTIVE", getCurrentUsername());
    }

    // ================ HELPER METHODS ================

    private String generateAccountNumber(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sequence = String.format("%04d", getNextAccountSequence());
        return prefix + "-" + date + "-" + sequence;
    }

    private String generateTransactionNumber() {
        String prefix = "CRTX";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%06d", getNextTransactionSequence());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextAccountSequence() {
        return (int) (creditAccountRepository.count() + 1);
    }

    private synchronized int getNextTransactionSequence() {
        return (int) (creditTransactionRepository.count() + 1);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
