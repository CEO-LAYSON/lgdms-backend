package com.crn.lgdms.modules.transfer.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.enums.TransactionStatus;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.common.exception.ValidationException;
import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.repository.EmptyLedgerRepository;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.inventory.service.InventoryService;        // NEW
import com.crn.lgdms.modules.inventory.service.StockQueryService;      // NEW
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.transfer.domain.entity.*;
import com.crn.lgdms.modules.transfer.dto.request.*;
import com.crn.lgdms.modules.transfer.dto.response.TransferRequestResponse;
import com.crn.lgdms.modules.transfer.dto.response.TransferResponse;
import com.crn.lgdms.modules.transfer.dto.mapper.*;
import com.crn.lgdms.modules.transfer.repository.*;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRepository transferRepository;
    private final TransferItemRepository transferItemRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final EmptyLedgerRepository emptyLedgerRepository;

    // NEW: Add inventory services
    private final StockQueryService stockQueryService;
    private final InventoryService inventoryService;

    private final TransferRequestMapper transferRequestMapper;
    private final TransferRequestItemMapper transferRequestItemMapper;
    private final TransferMapper transferMapper;
    private final TransferItemMapper transferItemMapper;

    private final AuditLogService auditLogService;

    // ================ TRANSFER REQUESTS ================

    @Transactional
    public TransferRequestResponse createTransferRequest(CreateTransferRequestRequest request) {
        log.info("Creating transfer request from {} to {}", request.getFromLocationId(), request.getToLocationId());

        // Validate locations
        Location fromLocation = locationRepository.findById(request.getFromLocationId())
            .orElseThrow(() -> new NotFoundException("From location not found with id: " + request.getFromLocationId()));

        Location toLocation = locationRepository.findById(request.getToLocationId())
            .orElseThrow(() -> new NotFoundException("To location not found with id: " + request.getToLocationId()));

        // Create request
        TransferRequest transferRequest = transferRequestMapper.toEntity(request);
        transferRequest.setFromLocation(fromLocation);
        transferRequest.setToLocation(toLocation);
        transferRequest.setRequestNumber(generateRequestNumber());
        transferRequest.setStatus(TransactionStatus.PENDING);

        // Process items
        List<TransferRequestItem> items = request.getItems().stream()
            .map(itemRequest -> createTransferRequestItem(itemRequest, transferRequest))
            .collect(Collectors.toList());

        transferRequest.setItems(items);

        TransferRequest saved = transferRequestRepository.save(transferRequest);

        auditLogService.log(AuditAction.CREATE, "TransferRequest", saved.getId(),
            null, saved.getRequestNumber(), request.getRequestedBy());

        log.info("Transfer request created with number: {}", saved.getRequestNumber());
        return transferRequestMapper.toResponse(saved);
    }

    private TransferRequestItem createTransferRequestItem(AddTransferRequestItemRequest itemRequest,
                                                          TransferRequest transferRequest) {
        CylinderSize cylinderSize = cylinderSizeRepository.findById(itemRequest.getCylinderSizeId())
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + itemRequest.getCylinderSizeId()));

        TransferRequestItem item = transferRequestItemMapper.toEntity(itemRequest);
        item.setTransferRequest(transferRequest);
        item.setCylinderSize(cylinderSize);

        return item;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "transferRequests", key = "#id")
    public TransferRequestResponse getTransferRequestById(String id) {
        TransferRequest request = transferRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer request not found with id: " + id));
        return transferRequestMapper.toResponse(request);
    }

    @Transactional(readOnly = true)
    public TransferRequestResponse getTransferRequestByNumber(String requestNumber) {
        TransferRequest request = transferRequestRepository.findByRequestNumber(requestNumber)
            .orElseThrow(() -> new NotFoundException("Transfer request not found with number: " + requestNumber));
        return transferRequestMapper.toResponse(request);
    }

    @Transactional(readOnly = true)
    public Page<TransferRequestResponse> getAllTransferRequests(Pageable pageable) {
        return transferRequestRepository.findAll(pageable)
            .map(transferRequestMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> getPendingRequestsForLocation(String locationId) {
        return transferRequestRepository.findPendingRequestsForLocation(locationId).stream()
            .map(transferRequestMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "transferRequests", key = "#id")
    public TransferRequestResponse approveTransferRequest(String id, ApproveTransferRequest request) {
        log.info("Approving transfer request with ID: {}", id);

        TransferRequest transferRequest = transferRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer request not found with id: " + id));

        if (transferRequest.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Transfer request already processed");
        }

        // UPDATED: Now actually validates stock availability using Day 8 services
        validateStockAvailability(transferRequest, request);

        // Update approved quantities
        Map<String, Integer> approvedMap = new HashMap<>();
        if (request.getApprovedItems() != null) {
            approvedMap = request.getApprovedItems().stream()
                .collect(Collectors.toMap(
                    item -> item.getTransferRequestItemId(),
                    item -> item.getApprovedQuantity()
                ));
        }

        for (TransferRequestItem item : transferRequest.getItems()) {
            Integer approvedQty = approvedMap.getOrDefault(item.getId(), item.getRequestedQuantity());
            item.setApprovedQuantity(approvedQty);
        }

        transferRequest.setStatus(TransactionStatus.APPROVED);
        transferRequest.setReviewedBy(request.getReviewedBy());
        transferRequest.setReviewedAt(LocalDateTime.now());
        transferRequest.setNotes(request.getNotes());

        TransferRequest updated = transferRequestRepository.save(transferRequest);

        auditLogService.log(AuditAction.APPROVE, "TransferRequest", id,
            "PENDING", "APPROVED", request.getReviewedBy());

        return transferRequestMapper.toResponse(updated);
    }

    /**
     * UPDATED: Actual stock validation using Day 8 services
     */
    private void validateStockAvailability(TransferRequest request, ApproveTransferRequest approveRequest) {
        log.info("Validating stock availability for transfer request: {}", request.getRequestNumber());

        // Create map of approved quantities
        Map<String, Integer> approvedMap = new HashMap<>();
        if (approveRequest.getApprovedItems() != null) {
            approvedMap = approveRequest.getApprovedItems().stream()
                .collect(Collectors.toMap(
                    item -> item.getTransferRequestItemId(),
                    item -> item.getApprovedQuantity()
                ));
        }

        // Check each item
        for (TransferRequestItem item : request.getItems()) {
            int requestedQty = approvedMap.getOrDefault(item.getId(), item.getRequestedQuantity());

            if (requestedQty <= 0) continue; // Skip if not approving this item

            // Use StockQueryService from Day 8 to get current stock
            Integer availableStock = stockQueryService.getCurrentStock(
                request.getFromLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType()
            );

            // Use InventoryService from Day 8 to validate no negative stock
            inventoryService.validateStockMovement(
                request.getFromLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType(),
                -requestedQty // Negative because stock will decrease
            );

            if (availableStock == null || availableStock < requestedQty) {
                throw new ValidationException(
                    String.format("Insufficient stock at %s for %s %s. Available: %d, Requested: %d",
                        request.getFromLocation().getName(),
                        item.getCylinderSize().getName(),
                        item.getProductType(),
                        availableStock != null ? availableStock : 0,
                        requestedQty)
                );
            }

            log.debug("Stock validated for {} {}: {} available, {} requested",
                item.getCylinderSize().getName(),
                item.getProductType(),
                availableStock,
                requestedQty);
        }
    }

    @Transactional
    @CacheEvict(value = "transferRequests", key = "#id")
    public TransferRequestResponse rejectTransferRequest(String id, RejectTransferRequest request) {
        log.info("Rejecting transfer request with ID: {}", id);

        TransferRequest transferRequest = transferRequestRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer request not found with id: " + id));

        if (transferRequest.getStatus() != TransactionStatus.PENDING) {
            throw new ConflictException("Transfer request already processed");
        }

        transferRequest.setStatus(TransactionStatus.REJECTED);
        transferRequest.setReviewedBy(request.getReviewedBy());
        transferRequest.setReviewedAt(LocalDateTime.now());
        transferRequest.setRejectionReason(request.getRejectionReason());

        TransferRequest updated = transferRequestRepository.save(transferRequest);

        auditLogService.log(AuditAction.REJECT, "TransferRequest", id,
            "PENDING", "REJECTED", request.getReviewedBy());

        return transferRequestMapper.toResponse(updated);
    }

    // ================ TRANSFERS ================

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {
        log.info("Creating transfer from request ID: {}", request.getTransferRequestId());

        TransferRequest transferRequest = transferRequestRepository.findById(request.getTransferRequestId())
            .orElseThrow(() -> new NotFoundException("Transfer request not found with id: " + request.getTransferRequestId()));

        if (transferRequest.getStatus() != TransactionStatus.APPROVED) {
            throw new ConflictException("Transfer request must be approved before creating transfer");
        }

        if (transferRepository.findByTransferRequestId(transferRequest.getId()).isPresent()) {
            throw new ConflictException("Transfer already created for this request");
        }

        // Create transfer
        Transfer transfer = Transfer.builder()
            .transferNumber(generateTransferNumber())
            .transferRequest(transferRequest)
            .fromLocation(transferRequest.getFromLocation())
            .toLocation(transferRequest.getToLocation())
            .transferDate(request.getTransferDate())
            .dispatchedBy(request.getDispatchedBy())
            .dispatchedAt(LocalDateTime.now())
            .status(TransactionStatus.IN_TRANSIT)
            .notes(request.getNotes())
            .build();

        // Create transfer items based on approved quantities
        List<TransferItem> items = transferRequest.getItems().stream()
            .filter(item -> item.getApprovedQuantity() != null && item.getApprovedQuantity() > 0)
            .map(item -> createTransferItem(item, transfer))
            .collect(Collectors.toList());

        transfer.setItems(items);

        // Update stock ledger for outgoing stock
        for (TransferItem item : items) {
            createOutgoingStockLedger(item, transfer);
        }

        Transfer saved = transferRepository.save(transfer);

        auditLogService.log(AuditAction.CREATE, "Transfer", saved.getId(),
            null, saved.getTransferNumber(), request.getDispatchedBy());

        log.info("Transfer created with number: {}", saved.getTransferNumber());
        return transferMapper.toResponse(saved);
    }

    private TransferItem createTransferItem(TransferRequestItem requestItem, Transfer transfer) {
        return TransferItem.builder()
            .transfer(transfer)
            .cylinderSize(requestItem.getCylinderSize())
            .productType(requestItem.getProductType())
            .quantity(requestItem.getApprovedQuantity())
            .notes(requestItem.getNotes())
            .build();
    }

    private void createOutgoingStockLedger(TransferItem item, Transfer transfer) {
        StockLedger ledger = StockLedger.builder()
            .location(transfer.getFromLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.TRANSFER_OUT)
            .quantity(-item.getQuantity()) // Negative for outgoing
            .runningBalance(getCurrentStock(transfer.getFromLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType()) - item.getQuantity())
            .referenceType("TRANSFER")
            .referenceId(transfer.getId())
            .referenceNumber(transfer.getTransferNumber())
            .notes("Transferred to: " + transfer.getToLocation().getName())
            .build();

        stockLedgerRepository.save(ledger);
        item.setOutgoingStockLedger(ledger);
    }

    @Transactional
    public TransferResponse receiveTransfer(String id, ReceiveTransferRequest request) {
        log.info("Receiving transfer with ID: {}", id);

        Transfer transfer = transferRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer not found with id: " + id));

        if (transfer.getStatus() != TransactionStatus.IN_TRANSIT) {
            throw new ConflictException("Transfer is not in transit");
        }

        // Process received items
        Map<String, ReceiveTransferRequest.ReceivedItem> receivedMap = new HashMap<>();
        if (request.getReceivedItems() != null) {
            receivedMap = request.getReceivedItems().stream()
                .collect(Collectors.toMap(
                    item -> item.getTransferItemId(),
                    item -> item
                ));
        }

        for (TransferItem item : transfer.getItems()) {
            ReceiveTransferRequest.ReceivedItem received = receivedMap.get(item.getId());
            int receivedQty = received != null ? received.getReceivedQuantity() : item.getQuantity();

            // CRITICAL: Enforce refill rule
            if (item.getProductType() == ProductType.REFILL) {
                int emptyReturned = received != null ? received.getEmptyReturnedQuantity() : 0;
                if (emptyReturned < receivedQty) {
                    throw new ValidationException(
                        String.format("Refill requires empty cylinder. Expected %d empties, got %d",
                            receivedQty, emptyReturned)
                    );
                }
                // Create empty ledger entry
                createEmptyLedgerEntry(item, transfer, emptyReturned);
            }

            // Create incoming stock ledger
            createIncomingStockLedger(item, transfer, receivedQty);
        }

        transfer.setStatus(TransactionStatus.COMPLETED);
        transfer.setReceivedBy(request.getReceivedBy());
        transfer.setReceivedAt(LocalDateTime.now());
        transfer.setNotes(transfer.getNotes() + " | Received: " + request.getNotes());

        Transfer updated = transferRepository.save(transfer);

        auditLogService.log(AuditAction.UPDATE, "Transfer", id,
            "IN_TRANSIT", "COMPLETED", request.getReceivedBy());

        log.info("Transfer completed: {}", updated.getTransferNumber());
        return transferMapper.toResponse(updated);
    }

    private void createIncomingStockLedger(TransferItem item, Transfer transfer, int receivedQty) {
        int currentStock = getCurrentStock(transfer.getToLocation().getId(),
            item.getCylinderSize().getId(),
            item.getProductType());

        StockLedger ledger = StockLedger.builder()
            .location(transfer.getToLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.TRANSFER_IN)
            .quantity(receivedQty)
            .runningBalance(currentStock + receivedQty)
            .referenceType("TRANSFER")
            .referenceId(transfer.getId())
            .referenceNumber(transfer.getTransferNumber())
            .batchNumber(item.getBatchNumber())
            .notes("Received from: " + transfer.getFromLocation().getName())
            .build();

        stockLedgerRepository.save(ledger);
        item.setIncomingStockLedger(ledger);
    }

    private void createEmptyLedgerEntry(TransferItem item, Transfer transfer, int emptyReturned) {
        int currentEmptyBalance = getCurrentEmptyBalance(transfer.getToLocation().getId(),
            item.getCylinderSize().getId());

        EmptyLedger ledger = EmptyLedger.builder()
            .location(transfer.getToLocation())
            .cylinderSize(item.getCylinderSize())
            .movementType(MovementType.TRANSFER_IN)
            .quantity(emptyReturned)
            .runningBalance(currentEmptyBalance + emptyReturned)
            .referenceType("TRANSFER")
            .referenceId(transfer.getId())
            .referenceNumber(transfer.getTransferNumber())
            .notes("Empty cylinders returned with refill transfer")
            .build();

        emptyLedgerRepository.save(ledger);
        item.setEmptyLedger(ledger);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "transfers", key = "#id")
    public TransferResponse getTransferById(String id) {
        Transfer transfer = transferRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer not found with id: " + id));
        return transferMapper.toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransferByNumber(String transferNumber) {
        Transfer transfer = transferRepository.findByTransferNumber(transferNumber)
            .orElseThrow(() -> new NotFoundException("Transfer not found with number: " + transferNumber));
        return transferMapper.toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfers(String fromLocationId, String toLocationId,
                                               TransactionStatus status, Pageable pageable) {
        return transferRepository.findTransfers(fromLocationId, toLocationId, status, pageable)
            .map(transferMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "transfers", key = "#id")
    public void cancelTransfer(String id, String reason) {
        log.info("Cancelling transfer with ID: {}", id);

        Transfer transfer = transferRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Transfer not found with id: " + id));

        if (transfer.getStatus() != TransactionStatus.IN_TRANSIT) {
            throw new ConflictException("Cannot cancel transfer with status: " + transfer.getStatus());
        }

        // Reverse outgoing stock ledger entries
        for (TransferItem item : transfer.getItems()) {
            reverseOutgoingStock(item, transfer);
        }

        transfer.setStatus(TransactionStatus.CANCELLED);
        transfer.setNotes(transfer.getNotes() + " | Cancelled: " + reason);

        transferRepository.save(transfer);

        auditLogService.log(AuditAction.DELETE, "Transfer", id,
            transfer.getTransferNumber(), "CANCELLED", getCurrentUsername());
    }

    private void reverseOutgoingStock(TransferItem item, Transfer transfer) {
        // Create reversal entry in stock ledger
        StockLedger reversal = StockLedger.builder()
            .location(transfer.getFromLocation())
            .cylinderSize(item.getCylinderSize())
            .productType(item.getProductType())
            .movementType(MovementType.ADJUSTMENT)
            .quantity(item.getQuantity()) // Positive to add back
            .runningBalance(getCurrentStock(transfer.getFromLocation().getId(),
                item.getCylinderSize().getId(),
                item.getProductType()) + item.getQuantity())
            .referenceType("TRANSFER_CANCELLED")
            .referenceId(transfer.getId())
            .referenceNumber(transfer.getTransferNumber())
            .notes("Stock reversed due to transfer cancellation")
            .build();

        stockLedgerRepository.save(reversal);
    }

    // Helper methods
    private int getCurrentStock(String locationId, String cylinderSizeId, ProductType productType) {
        Integer stock = stockLedgerRepository.getCurrentStock(locationId, cylinderSizeId, productType);
        return stock != null ? stock : 0;
    }

    private int getCurrentEmptyBalance(String locationId, String cylinderSizeId) {
        Integer balance = emptyLedgerRepository.getCurrentEmptyBalance(locationId, cylinderSizeId);
        return balance != null ? balance : 0;
    }

    private String generateRequestNumber() {
        String prefix = "TRQ";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", getNextRequestSequenceNumber());
        return prefix + "-" + date + "-" + sequence;
    }

    private String generateTransferNumber() {
        String prefix = "TRF";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", getNextTransferSequenceNumber());
        return prefix + "-" + date + "-" + sequence;
    }

    private synchronized int getNextRequestSequenceNumber() {
        LocalDate today = LocalDate.now();
        long count = transferRequestRepository.findByDateRange(today, today).size();
        return (int) (count + 1);
    }

    private synchronized int getNextTransferSequenceNumber() {
        LocalDate today = LocalDate.now();
        long count = transferRepository.findByDateRange(today, today).size();
        return (int) (count + 1);
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
