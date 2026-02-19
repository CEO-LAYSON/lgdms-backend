package com.crn.lgdms.modules.receiving.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.masterdata.repository.SupplierRepository;
import com.crn.lgdms.modules.receiving.domain.entity.GoodsReceiving;
import com.crn.lgdms.modules.receiving.domain.entity.ReceivingItem;
import com.crn.lgdms.modules.receiving.dto.request.AddReceivingItemRequest;
import com.crn.lgdms.modules.receiving.dto.request.CreateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.request.UpdateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.request.VerifyReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.response.ReceivingResponse;
import com.crn.lgdms.modules.receiving.dto.mapper.ReceivingItemMapper;
import com.crn.lgdms.modules.receiving.dto.mapper.ReceivingMapper;
import com.crn.lgdms.modules.receiving.repository.GoodsReceivingRepository;
import com.crn.lgdms.modules.receiving.repository.ReceivingItemRepository;
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
public class ReceivingService {

    private final GoodsReceivingRepository receivingRepository;
    private final ReceivingItemRepository receivingItemRepository;
    private final SupplierRepository supplierRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final ReceivingMapper receivingMapper;
    private final ReceivingItemMapper receivingItemMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public ReceivingResponse createReceiving(CreateReceivingRequest request) {
        log.info("Creating new goods receiving from supplier: {}", request.getSupplierId());

        // Validate supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
            .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + request.getSupplierId()));

        // Validate location (should be HQ usually)
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + request.getLocationId()));

        // Create receiving document
        GoodsReceiving receiving = receivingMapper.toEntity(request);
        receiving.setSupplier(supplier);
        receiving.setLocation(location);
        receiving.setReceivingNumber(generateReceivingNumber());
        receiving.setStatus(TransactionStatus.PENDING);

        // Process items
        List<ReceivingItem> items = request.getItems().stream()
            .map(itemRequest -> createReceivingItem(itemRequest, receiving))
            .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalAmount = items.stream()
            .map(ReceivingItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = items.stream()
            .mapToInt(ReceivingItem::getQuantity)
            .sum();

        receiving.setTotalAmount(totalAmount);
        receiving.setTotalQuantity(totalQuantity);
        receiving.setItems(items);

        GoodsReceiving saved = receivingRepository.save(receiving);

        auditLogService.log(AuditAction.CREATE, "GoodsReceiving", saved.getId(),
            null, saved.getReceivingNumber(), getCurrentUsername());

        log.info("Goods receiving created with number: {}", saved.getReceivingNumber());
        return receivingMapper.toResponse(saved);
    }

    private ReceivingItem createReceivingItem(AddReceivingItemRequest itemRequest, GoodsReceiving receiving) {
        CylinderSize cylinderSize = cylinderSizeRepository.findById(itemRequest.getCylinderSizeId())
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + itemRequest.getCylinderSizeId()));

        ReceivingItem item = receivingItemMapper.toEntity(itemRequest);
        item.setGoodsReceiving(receiving);
        item.setCylinderSize(cylinderSize);
        item.setTotalPrice(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

        return item;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "receivings", key = "#id")
    public ReceivingResponse getReceivingById(String id) {
        GoodsReceiving receiving = receivingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Goods receiving not found with id: " + id));
        return receivingMapper.toResponse(receiving);
    }

    @Transactional(readOnly = true)
    public ReceivingResponse getReceivingByNumber(String receivingNumber) {
        GoodsReceiving receiving = receivingRepository.findByReceivingNumber(receivingNumber)
            .orElseThrow(() -> new NotFoundException("Goods receiving not found with number: " + receivingNumber));
        return receivingMapper.toResponse(receiving);
    }

    @Transactional(readOnly = true)
    public Page<ReceivingResponse> getAllReceivings(Pageable pageable) {
        return receivingRepository.findAll(pageable)
            .map(receivingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReceivingResponse> searchReceivings(String searchTerm, Pageable pageable) {
        return receivingRepository.searchReceivings(searchTerm, pageable)
            .map(receivingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReceivingResponse> getReceivingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return receivingRepository.findByDateRange(startDate, endDate).stream()
            .map(receivingMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "receivings", key = "#id")
    public ReceivingResponse updateReceiving(String id, UpdateReceivingRequest request) {
        log.info("Updating goods receiving with ID: {}", id);

        GoodsReceiving receiving = receivingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Goods receiving not found with id: " + id));

        // Only allow update if status is PENDING
        if (receiving.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Cannot update receiving with status: " + receiving.getStatus());
        }

        receivingMapper.updateEntity(request, receiving);
        GoodsReceiving updated = receivingRepository.save(receiving);

        auditLogService.log(AuditAction.UPDATE, "GoodsReceiving", id,
            null, updated.getReceivingNumber(), getCurrentUsername());

        return receivingMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "receivings", key = "#id")
    public ReceivingResponse verifyReceiving(String id, VerifyReceivingRequest request) {
        log.info("Verifying goods receiving with ID: {}", id);

        GoodsReceiving receiving = receivingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Goods receiving not found with id: " + id));

        if (receiving.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Receiving already verified");
        }

        // Update stock ledger for each item
        for (ReceivingItem item : receiving.getItems()) {
            createStockLedgerEntry(item, receiving);
        }

        receiving.setStatus(TransactionStatus.COMPLETED);
        receiving.setVerifiedBy(request.getVerifiedBy());
        receiving.setVerifiedAt(LocalDateTime.now());

        GoodsReceiving verified = receivingRepository.save(receiving);

        auditLogService.log(AuditAction.APPROVE, "GoodsReceiving", id,
            "PENDING", "COMPLETED", getCurrentUsername());

        log.info("Goods receiving verified: {}", verified.getReceivingNumber());
        return receivingMapper.toResponse(verified);
    }

    private void createStockLedgerEntry(ReceivingItem item, GoodsReceiving receiving) {
        StockLedger ledger = StockLedger.builder()
            .location(receiving.getLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.RECEIVING)
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalValue(item.getTotalPrice())
            .referenceType("GOODS_RECEIVING")
            .referenceId(receiving.getId())
            .referenceNumber(receiving.getReceivingNumber())
            .batchNumber(item.getBatchNumber())
            .expiryDate(item.getExpiryDate())
            .notes("Goods received from: " + receiving.getSupplier().getName())
            .build();

        stockLedgerRepository.save(ledger);
        item.setStockLedger(ledger);
    }

    @Transactional
    @CacheEvict(value = "receivings", key = "#id")
    public void cancelReceiving(String id, String reason) {
        log.info("Cancelling goods receiving with ID: {}", id);

        GoodsReceiving receiving = receivingRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Goods receiving not found with id: " + id));

        if (receiving.getStatus() == TransactionStatus.COMPLETED) {
            throw new ConflictException("Cannot cancel completed receiving");
        }

        receiving.setStatus(TransactionStatus.CANCELLED);
        receiving.setNotes(receiving.getNotes() + " | Cancelled: " + reason);
        receivingRepository.save(receiving);

        auditLogService.log(AuditAction.DELETE, "GoodsReceiving", id,
            receiving.getReceivingNumber(), "CANCELLED", getCurrentUsername());
    }

    private String generateReceivingNumber() {
        String prefix = "GR";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", getNextSequenceNumber());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextSequenceNumber() {
        LocalDate today = LocalDate.now();
        long count = receivingRepository.findByDateRange(today, today).size();
        return (int) (count + 1);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
