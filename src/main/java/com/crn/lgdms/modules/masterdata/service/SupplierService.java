package com.crn.lgdms.modules.masterdata.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import com.crn.lgdms.modules.masterdata.dto.request.CreateSupplierRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdateSupplierRequest;
import com.crn.lgdms.modules.masterdata.dto.response.SupplierResponse;
import com.crn.lgdms.modules.masterdata.dto.mapper.SupplierMapper;
import com.crn.lgdms.modules.masterdata.repository.SupplierRepository;
import com.crn.lgdms.modules.users.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.info("Creating new supplier: {}", request.getName());

        if (supplierRepository.existsByName(request.getName())) {
            throw new ConflictException("Supplier already exists: " + request.getName());
        }

        if (request.getCode() != null && supplierRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Supplier code already exists: " + request.getCode());
        }

        Supplier supplier = supplierMapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);

        auditLogService.log(AuditAction.CREATE, "Supplier", saved.getId(),
            null, saved.getName(), getCurrentUsername());

        return supplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "suppliers", key = "#id")
    public SupplierResponse getSupplierById(String id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));
        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue().stream()
            .map(supplierMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliers(String searchTerm, Pageable pageable) {
        return supplierRepository.searchSuppliers(searchTerm, pageable)
            .map(supplierMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "suppliers", key = "#id")
    public SupplierResponse updateSupplier(String id, UpdateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(supplier.getName())) {
            if (supplierRepository.existsByName(request.getName())) {
                throw new ConflictException("Supplier already exists: " + request.getName());
            }
        }

        if (request.getCode() != null && !request.getCode().equals(supplier.getCode())) {
            if (supplierRepository.existsByCode(request.getCode())) {
                throw new ConflictException("Supplier code already exists: " + request.getCode());
            }
        }

        supplierMapper.updateEntity(request, supplier);
        Supplier updated = supplierRepository.save(supplier);

        auditLogService.log(AuditAction.UPDATE, "Supplier", id,
            null, updated.getName(), getCurrentUsername());

        return supplierMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "suppliers", key = "#id")
    public void deleteSupplier(String id) {
        log.info("Deleting supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Supplier not found with id: " + id));

        supplierRepository.delete(supplier);

        auditLogService.log(AuditAction.DELETE, "Supplier", id,
            supplier.getName(), null, getCurrentUsername());
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
