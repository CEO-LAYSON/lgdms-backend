package com.crn.lgdms.modules.sales.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.common.enums.PaymentMethod;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.common.exception.ValidationException;
import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
import com.crn.lgdms.modules.credit.repository.CreditAccountRepository;
import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.repository.EmptyLedgerRepository;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.inventory.service.InventoryService;        // NEW
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.sales.domain.entity.*;
import com.crn.lgdms.modules.sales.dto.request.CreateSaleRequest;
import com.crn.lgdms.modules.sales.dto.response.SaleResponse;
import com.crn.lgdms.modules.sales.dto.mapper.SaleMapper;
import com.crn.lgdms.modules.sales.repository.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final CustomerRepository customerRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final EmptyLedgerRepository emptyLedgerRepository;
    private final CreditAccountRepository creditAccountRepository;
    private final InvoiceNumberSequenceRepository invoiceNumberSequenceRepository;

    // NEW: Add inventory service
    private final InventoryService inventoryService;

    private final SaleMapper saleMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        log.info("Creating sale at location: {}", request.getLocationId());

        // Validate location
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + request.getLocationId()));

        // Validate sales person
        User salesPerson = userRepository.findById(request.getSalesPersonId())
            .orElseThrow(() -> new NotFoundException("Sales person not found with id: " + request.getSalesPersonId()));

        // Validate customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + request.getCustomerId()));

            // Check credit limit if this is a credit sale
            boolean isCreditSale = request.getPayments() == null || request.getPayments().isEmpty();
            if (isCreditSale && customer.getCreditLimit() != null) {
                BigDecimal newBalance = customer.getCurrentBalance().add(calculateTotal(request));
                if (newBalance.compareTo(customer.getCreditLimit()) > 0) {
                    throw new ValidationException(
                        String.format("Credit limit exceeded. Limit: %s, New balance: %s",
                            customer.getCreditLimit(), newBalance)
                    );
                }
            }
        }

        // Create sale
        Sale sale = Sale.builder()
            .invoiceNumber(generateInvoiceNumber(location.getId(), request.getSaleDate()))
            .location(location)
            .customer(customer)
            .salesPerson(salesPerson)
            .saleDate(request.getSaleDate())
            .saleTime(LocalDateTime.now())
            .discount(request.getDiscount())
            .tax(request.getTax())
            .status(TransactionStatus.COMPLETED)
            .notes(request.getNotes())
            .build();

        // Calculate totals and create items
        List<SaleItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateSaleRequest.SaleItemRequest itemRequest : request.getItems()) {
            SaleItem item = createSaleItem(itemRequest, sale);
            items.add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        // CRITICAL: Enforce refill rule - empty cylinder collection
        validateRefillRule(items);

        // UPDATED: Validate stock availability BEFORE processing sale
        validateStockAvailability(items, sale);

        BigDecimal total = subtotal.subtract(request.getDiscount()).add(request.getTax());

        // Process payments
        List<SalePayment> payments = new ArrayList<>();
        BigDecimal paidAmount = BigDecimal.ZERO;

        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            for (CreateSaleRequest.SalePaymentRequest paymentRequest : request.getPayments()) {
                SalePayment payment = createSalePayment(paymentRequest, sale);
                payments.add(payment);
                paidAmount = paidAmount.add(payment.getAmount());
            }
        }

        sale.setSubtotal(subtotal);
        sale.setTotalAmount(total);
        sale.setPaidAmount(paidAmount);
        sale.setBalanceDue(total.subtract(paidAmount));
        sale.setCreditSale(paidAmount.compareTo(total) < 0);
        sale.setItems(items);
        sale.setPayments(payments);

        // Update stock for each item
        for (SaleItem item : items) {
            updateStockForSale(item, sale);

            // Handle empty cylinder collection for refills
            if (item.getProductType() == ProductType.REFILL && item.getEmptyReturned()) {
                updateEmptyCylinderBalance(item, sale);
            }
        }

        // Update customer balance if credit sale
        if (sale.isCreditSale() && customer != null) {
            customer.setCurrentBalance(customer.getCurrentBalance().add(sale.getBalanceDue()));
            customerRepository.save(customer);

            // Create credit account transaction
            createCreditTransaction(sale);
        }

        Sale saved = saleRepository.save(sale);

        auditLogService.log(AuditAction.CREATE, "Sale", saved.getId(),
            null, saved.getInvoiceNumber(), salesPerson.getUsername());

        log.info("Sale completed with invoice: {}", saved.getInvoiceNumber());
        return saleMapper.toResponse(saved);
    }

    /**
     * NEW: Validate stock availability before sale
     */
    private void validateStockAvailability(List<SaleItem> items, Sale sale) {
        for (SaleItem item : items) {
            // Use InventoryService to validate no negative stock
            inventoryService.validateStockMovement(
                sale.getLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType(),
                -item.getQuantity() // Negative because stock will decrease
            );

            // Check if enough stock exists
            Integer currentStock = stockLedgerRepository.getCurrentStock(
                sale.getLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType()
            );

            if (currentStock == null || currentStock < item.getQuantity()) {
                throw new ValidationException(
                    String.format("Insufficient stock at %s for %s %s. Available: %d, Requested: %d",
                        sale.getLocation().getName(),
                        item.getCylinderSize().getName(),
                        item.getProductType(),
                        currentStock != null ? currentStock : 0,
                        item.getQuantity())
                );
            }
        }
    }

    private SaleItem createSaleItem(CreateSaleRequest.SaleItemRequest request, Sale sale) {
        CylinderSize cylinderSize = cylinderSizeRepository.findById(request.getCylinderSizeId())
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + request.getCylinderSizeId()));

        BigDecimal itemTotal = request.getUnitPrice()
            .multiply(BigDecimal.valueOf(request.getQuantity()))
            .subtract(request.getDiscount());

        return SaleItem.builder()
            .sale(sale)
            .cylinderSize(cylinderSize)
            .productType(request.getProductType())
            .quantity(request.getQuantity())
            .unitPrice(request.getUnitPrice())
            .discount(request.getDiscount())
            .totalPrice(itemTotal)
            .emptyReturned(request.getEmptyReturned())
            .emptyQuantity(request.getEmptyReturned() ? request.getQuantity() : 0)
            .notes(request.getNotes())
            .build();
    }

    private SalePayment createSalePayment(CreateSaleRequest.SalePaymentRequest request, Sale sale) {
        return SalePayment.builder()
            .sale(sale)
            .paymentMethod(request.getPaymentMethod())
            .amount(request.getAmount())
            .referenceNumber(request.getReferenceNumber())
            .paymentDate(LocalDateTime.now())
            .receivedBy(sale.getSalesPerson().getUsername())
            .notes(request.getNotes())
            .build();
    }

    private void validateRefillRule(List<SaleItem> items) {
        for (SaleItem item : items) {
            if (item.getProductType() == ProductType.REFILL && !item.getEmptyReturned()) {
                throw new ValidationException(
                    String.format("Refill of %s %s requires empty cylinder return",
                        item.getQuantity(), item.getCylinderSize().getName())
                );
            }
        }
    }

    private void updateStockForSale(SaleItem item, Sale sale) {
        // Get current stock before sale
        Integer currentStock = stockLedgerRepository.getCurrentStock(
            sale.getLocation().getId(),
            item.getCylinderSize().getId(),
            item.getProductType()
        );

        if (currentStock == null || currentStock < item.getQuantity()) {
            throw new ValidationException(
                String.format("Insufficient stock at %s. Available: %d, Requested: %d",
                    sale.getLocation().getName(),
                    currentStock != null ? currentStock : 0,
                    item.getQuantity())
            );
        }

        // Create stock ledger entry (negative for sale)
        StockLedger ledger = StockLedger.builder()
            .location(sale.getLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.SALE)
            .quantity(-item.getQuantity())
            .runningBalance(currentStock - item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalValue(item.getTotalPrice())
            .referenceType("SALE")
            .referenceId(sale.getId())
            .referenceNumber(sale.getInvoiceNumber())
            .batchNumber(item.getBatchNumber())
            .notes("Sale to: " + (sale.getCustomer() != null ? sale.getCustomer().getName() : "Walk-in"))
            .build();

        stockLedgerRepository.save(ledger);
        item.setStockLedger(ledger);
    }

    private void updateEmptyCylinderBalance(SaleItem item, Sale sale) {
        Integer currentEmptyBalance = emptyLedgerRepository.getCurrentEmptyBalance(
            sale.getLocation().getId(),
            item.getCylinderSize().getId()
        );

        EmptyLedger ledger = EmptyLedger.builder()
            .location(sale.getLocation())
            .cylinderSize(item.getCylinderSize())
            .movementType(MovementType.SALE)
            .quantity(item.getQuantity()) // Positive because we're receiving empties
            .runningBalance((currentEmptyBalance != null ? currentEmptyBalance : 0) + item.getQuantity())
            .referenceType("SALE")
            .referenceId(sale.getId())
            .referenceNumber(sale.getInvoiceNumber())
            .notes("Empty cylinders collected from sale")
            .build();

        emptyLedgerRepository.save(ledger);
        item.setEmptyLedger(ledger);
    }

    private void createCreditTransaction(Sale sale) {
        CreditAccount creditAccount = creditAccountRepository.findByCustomerId(sale.getCustomer().getId())
            .orElseGet(() -> {
                CreditAccount newAccount = CreditAccount.builder()
                    .customer(sale.getCustomer())
                    .creditLimit(sale.getCustomer().getCreditLimit())
                    .currentBalance(BigDecimal.ZERO)
                    .build();
                return creditAccountRepository.save(newAccount);
            });

        // Credit transaction will be implemented in Day 11
    }

    private BigDecimal calculateTotal(CreateSaleRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateSaleRequest.SaleItemRequest item : request.getItems()) {
            subtotal = subtotal.add(
                item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .subtract(item.getDiscount())
            );
        }
        return subtotal.subtract(request.getDiscount()).add(request.getTax());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sales", key = "#id")
    public SaleResponse getSaleById(String id) {
        Sale sale = saleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSaleByInvoiceNumber(String invoiceNumber) {
        Sale sale = saleRepository.findByInvoiceNumber(invoiceNumber)
            .orElseThrow(() -> new NotFoundException("Sale not found with invoice: " + invoiceNumber));
        return saleMapper.toResponse(sale);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> getAllSales(Pageable pageable) {
        return saleRepository.findAll(pageable)
            .map(saleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> getSalesByLocation(String locationId, Pageable pageable) {
        // Custom query needed
        return saleRepository.findByLocationId(locationId).stream()
            .map(saleMapper::toResponse)
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
    }

    @Transactional(readOnly = true)
    public Page<SaleResponse> getSalesByCustomer(String customerId, Pageable pageable) {
        return saleRepository.findByCustomerId(customerId).stream()
            .map(saleMapper::toResponse)
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
    }

    @Transactional
    @CacheEvict(value = "sales", key = "#id")
    public SaleResponse voidSale(String id, String reason) {
        log.info("Voiding sale with ID: {}", id);

        Sale sale = saleRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Sale not found with id: " + id));

        if (sale.getStatus() == TransactionStatus.VOID) {
            throw new ConflictException("Sale already voided");
        }

        // Reverse stock entries
        for (SaleItem item : sale.getItems()) {
            reverseStockForVoidedSale(item, sale);
        }

        // Reverse customer balance if credit sale
        if (sale.isCreditSale() && sale.getCustomer() != null) {
            sale.getCustomer().setCurrentBalance(
                sale.getCustomer().getCurrentBalance().subtract(sale.getBalanceDue())
            );
            customerRepository.save(sale.getCustomer());
        }

        sale.setStatus(TransactionStatus.VOID);
        sale.setNotes(sale.getNotes() + " | VOIDED: " + reason);

        Sale saved = saleRepository.save(sale);

        auditLogService.log(AuditAction.DELETE, "Sale", id,
            sale.getInvoiceNumber(), "VOIDED", getCurrentUsername());

        return saleMapper.toResponse(saved);
    }

    private void reverseStockForVoidedSale(SaleItem item, Sale sale) {
        Integer currentStock = stockLedgerRepository.getCurrentStock(
            sale.getLocation().getId(),
            item.getCylinderSize().getId(),
            item.getProductType()
        );

        // Add stock back
        StockLedger ledger = StockLedger.builder()
            .location(sale.getLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.ADJUSTMENT)
            .quantity(item.getQuantity())
            .runningBalance(currentStock + item.getQuantity())
            .referenceType("SALE_VOID")
            .referenceId(sale.getId())
            .referenceNumber(sale.getInvoiceNumber())
            .notes("Stock reversed due to voided sale")
            .build();

        stockLedgerRepository.save(ledger);

        // Remove empty cylinders if they were collected
        if (item.getEmptyLedger() != null) {
            Integer currentEmpty = emptyLedgerRepository.getCurrentEmptyBalance(
                sale.getLocation().getId(),
                item.getCylinderSize().getId()
            );

            EmptyLedger emptyLedger = EmptyLedger.builder()
                .location(sale.getLocation())
                .cylinderSize(item.getCylinderSize())
                .movementType(MovementType.ADJUSTMENT)
                .quantity(-item.getQuantity())
                .runningBalance(currentEmpty - item.getQuantity())
                .referenceType("SALE_VOID")
                .referenceId(sale.getId())
                .referenceNumber(sale.getInvoiceNumber())
                .notes("Empty cylinders removed due to voided sale")
                .build();

            emptyLedgerRepository.save(emptyLedger);
        }
    }

    private String generateInvoiceNumber(String locationId, LocalDate saleDate) {
        String locationCode = getLocationCode(locationId);
        String dateStr = saleDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer sequence = getNextSequenceNumber(locationId, saleDate);

        return String.format("INV-%s-%s-%04d", locationCode, dateStr, sequence);
    }

    private String getLocationCode(String locationId) {
        Location location = locationRepository.findById(locationId).orElse(null);
        if (location != null && location.getCode() != null) {
            return location.getCode();
        }
        return "XXX";
    }

    private synchronized Integer getNextSequenceNumber(String locationId, LocalDate saleDate) {
        Integer maxSequence = invoiceNumberSequenceRepository.getMaxSequenceForDate(locationId, saleDate);
        int nextSequence = (maxSequence != null ? maxSequence : 0) + 1;

        InvoiceNumberSequence sequence = InvoiceNumberSequence.builder()
            .locationId(locationId)
            .saleDate(saleDate)
            .sequenceNumber(nextSequence)
            .build();

        invoiceNumberSequenceRepository.save(sequence);
        return nextSequence;
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
