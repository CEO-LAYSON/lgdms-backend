package com.crn.lgdms.modules.inventory.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.BusinessRuleException;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.inventory.domain.entity.StockAdjustment;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.dto.request.CreateAdjustmentRequest;
import com.crn.lgdms.modules.inventory.dto.response.AdjustmentResponse;
import com.crn.lgdms.modules.inventory.dto.mapper.StockAdjustmentMapper;
import com.crn.lgdms.modules.inventory.repository.StockAdjustmentRepository;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockLedgerRepository stockLedgerRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockQueryService stockQueryService;
    private final StockAdjustmentMapper stockAdjustmentMapper;
    private final AuditLogService auditLogService;

    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Core business rule: NO NEGATIVE STOCK ALLOWED
     * This method validates any stock movement that would cause negative inventory
     */
    @Transactional
    public void validateStockMovement(String locationId, String cylinderSizeId,
                                      ProductType productType, int requestedQuantity) {
        Integer currentStock = stockQueryService.getCurrentStock(locationId, cylinderSizeId, productType);

        if (currentStock == null) {
            currentStock = 0;
        }

        // CRITICAL BUSINESS RULE: Cannot have negative stock
        if (currentStock + requestedQuantity < 0) {
            throw new BusinessRuleException(
                String.format("Cannot have negative stock. Current: %d, Requested change: %d, Result: %d",
                    currentStock, requestedQuantity, currentStock + requestedQuantity)
            );
        }
    }

    @Transactional
    @CacheEvict(value = {"onHandStock", "currentStock", "totalStockValue"}, allEntries = true)
    public AdjustmentResponse createAdjustment(CreateAdjustmentRequest request, String username) {
        log.info("Creating stock adjustment at location: {}", request.getLocationId());

        // Validate location
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new NotFoundException("Location not found: " + request.getLocationId()));

        // Validate cylinder size
        CylinderSize cylinderSize = cylinderSizeRepository.findById(request.getCylinderSizeId())
            .orElseThrow(() -> new NotFoundException("Cylinder size not found: " + request.getCylinderSizeId()));

        // Get current stock
        Integer currentStock = stockQueryService.getCurrentStock(
            request.getLocationId(),
            request.getCylinderSizeId(),
            request.getProductType()
        );

        if (currentStock == null) currentStock = 0;

        // Calculate difference
        int difference = request.getNewQuantity() - currentStock;

        // Create adjustment record
        StockAdjustment adjustment = stockAdjustmentMapper.toEntity(request);
        adjustment.setAdjustmentNumber(generateAdjustmentNumber());
        adjustment.setLocation(location);
        adjustment.setCylinderSize(cylinderSize);
        adjustment.setOldQuantity(currentStock);
        adjustment.setDifference(difference);

        // Auto-approve if difference is within threshold, otherwise pending
        if (Math.abs(difference) <= 10) {
            adjustment.setStatus(TransactionStatus.APPROVED);
            adjustment.setApprovedBy(username);
            adjustment.setApprovedAt(LocalDateTime.now());

            // Apply the adjustment to stock ledger
            applyAdjustmentToStock(adjustment);
        } else {
            adjustment.setStatus(TransactionStatus.PENDING);
        }

        StockAdjustment saved = stockAdjustmentRepository.save(adjustment);

        auditLogService.log(
            AuditAction.CREATE,
            "StockAdjustment",
            saved.getId(),
            null,
            String.format("Adjustment created: %s, difference: %d", saved.getAdjustmentNumber(), difference),
            username
        );

        log.info("Stock adjustment created with number: {}", saved.getAdjustmentNumber());
        return stockAdjustmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"onHandStock", "currentStock", "totalStockValue"}, allEntries = true)
    public AdjustmentResponse approveAdjustment(String adjustmentId, String approvedBy) {
        log.info("Approving stock adjustment: {}", adjustmentId);

        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId)
            .orElseThrow(() -> new NotFoundException("Adjustment not found: " + adjustmentId));

        if (adjustment.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Adjustment already processed");
        }

        adjustment.setStatus(TransactionStatus.APPROVED);
        adjustment.setApprovedBy(approvedBy);
        adjustment.setApprovedAt(LocalDateTime.now());

        // Apply the adjustment to stock ledger
        applyAdjustmentToStock(adjustment);

        StockAdjustment saved = stockAdjustmentRepository.save(adjustment);

        auditLogService.log(
            AuditAction.APPROVE,
            "StockAdjustment",
            adjustmentId,
            "PENDING",
            "APPROVED",
            approvedBy
        );

        return stockAdjustmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"onHandStock", "currentStock", "totalStockValue"}, allEntries = true)
    public AdjustmentResponse rejectAdjustment(String adjustmentId, String reason) {
        log.info("Rejecting stock adjustment: {}", adjustmentId);

        StockAdjustment adjustment = stockAdjustmentRepository.findById(adjustmentId)
            .orElseThrow(() -> new NotFoundException("Adjustment not found: " + adjustmentId));

        if (adjustment.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Adjustment already processed");
        }

        adjustment.setStatus(TransactionStatus.REJECTED);
        adjustment.setNotes(adjustment.getNotes() + " | Rejected: " + reason);

        StockAdjustment saved = stockAdjustmentRepository.save(adjustment);

        auditLogService.log(
            AuditAction.REJECT,
            "StockAdjustment",
            adjustmentId,
            "PENDING",
            "REJECTED",
            "SYSTEM"
        );

        return stockAdjustmentMapper.toResponse(saved);
    }

    private void applyAdjustmentToStock(StockAdjustment adjustment) {
        // Get current stock before adjustment
        Integer currentStock = stockQueryService.getCurrentStock(
            adjustment.getLocation().getId(),
            adjustment.getCylinderSize().getId(),
            adjustment.getProductType()
        );

        if (currentStock == null) currentStock = 0;

        // CRITICAL: Validate no negative stock
        int newStock = currentStock + adjustment.getDifference();
        if (newStock < 0) {
            throw new BusinessRuleException(
                String.format("Adjustment would cause negative stock. Current: %d, Change: %d, Result: %d",
                    currentStock, adjustment.getDifference(), newStock)
            );
        }

        // Create stock ledger entry
        StockLedger ledger = StockLedger.builder()
            .location(adjustment.getLocation())
            .cylinderSize(adjustment.getCylinderSize())
            .productType(adjustment.getProductType())
            .movementType(MovementType.ADJUSTMENT)
            .quantity(adjustment.getDifference())
            .runningBalance(newStock)
            .referenceType("STOCK_ADJUSTMENT")
            .referenceId(adjustment.getId())
            .referenceNumber(adjustment.getAdjustmentNumber())
            .notes(adjustment.getReason())
            .transactionDate(LocalDateTime.now())
            .build();

        stockLedgerRepository.save(ledger);
    }

    private String generateAdjustmentNumber() {
        String prefix = "ADJ";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", getNextSequenceNumber());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextSequenceNumber() {
        return (int) (stockAdjustmentRepository.count() + 1);
    }
}
