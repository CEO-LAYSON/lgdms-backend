package com.crn.lgdms.modules.masterdata.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.domain.entity.PriceCategory;
import com.crn.lgdms.modules.masterdata.dto.request.CreatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.response.PriceCategoryResponse;
import com.crn.lgdms.modules.masterdata.dto.mapper.PriceCategoryMapper;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import com.crn.lgdms.modules.masterdata.repository.PriceCategoryRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCategoryService {

    private final PriceCategoryRepository priceCategoryRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final PriceCategoryMapper priceCategoryMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public PriceCategoryResponse createPriceCategory(CreatePriceCategoryRequest request) {
        log.info("Creating new price category: {}", request.getName());

        CylinderSize cylinderSize = cylinderSizeRepository.findById(request.getCylinderSizeId())
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + request.getCylinderSizeId()));

        PriceCategory priceCategory = priceCategoryMapper.toEntity(request);
        priceCategory.setCylinderSize(cylinderSize);

        PriceCategory saved = priceCategoryRepository.save(priceCategory);

        auditLogService.log(AuditAction.CREATE, "PriceCategory", saved.getId(),
            null, saved.getName(), getCurrentUsername());

        return priceCategoryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "priceCategories", key = "#id")
    public PriceCategoryResponse getPriceCategoryById(String id) {
        PriceCategory priceCategory = priceCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Price category not found with id: " + id));
        return priceCategoryMapper.toResponse(priceCategory);
    }

    @Transactional(readOnly = true)
    public Page<PriceCategoryResponse> searchPriceCategories(
        String name, String cylinderSizeId, ProductType productType,
        Boolean isActive, Pageable pageable) {

        return priceCategoryRepository.searchPriceCategories(
                name, cylinderSizeId, productType, isActive, pageable)
            .map(priceCategoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PriceCategoryResponse> getCurrentPrices(String cylinderSizeId) {
        CylinderSize cylinderSize = cylinderSizeRepository.findById(cylinderSizeId)
            .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + cylinderSizeId));

        return priceCategoryRepository.findByCylinderSizeAndIsActiveTrue(cylinderSize)
            .stream()
            .map(priceCategoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PriceCategoryResponse getCurrentPrice(String cylinderSizeId, ProductType productType) {
        return priceCategoryRepository.findCurrentPrice(cylinderSizeId, productType, LocalDate.now())
            .map(priceCategoryMapper::toResponse)
            .orElseThrow(() -> new NotFoundException(
                "No active price found for cylinder size " + cylinderSizeId + " and product type " + productType));
    }

    @Transactional
    @CacheEvict(value = "priceCategories", key = "#id")
    public PriceCategoryResponse updatePriceCategory(String id, UpdatePriceCategoryRequest request) {
        log.info("Updating price category with ID: {}", id);

        PriceCategory priceCategory = priceCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Price category not found with id: " + id));

        if (request.getCylinderSizeId() != null) {
            CylinderSize cylinderSize = cylinderSizeRepository.findById(request.getCylinderSizeId())
                .orElseThrow(() -> new NotFoundException("Cylinder size not found with id: " + request.getCylinderSizeId()));
            priceCategory.setCylinderSize(cylinderSize);
        }

        priceCategoryMapper.updateEntity(request, priceCategory);
        PriceCategory updated = priceCategoryRepository.save(priceCategory);

        auditLogService.log(AuditAction.UPDATE, "PriceCategory", id,
            null, updated.getName(), getCurrentUsername());

        return priceCategoryMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "priceCategories", key = "#id")
    public void deletePriceCategory(String id) {
        log.info("Deleting price category with ID: {}", id);

        PriceCategory priceCategory = priceCategoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Price category not found with id: " + id));

        priceCategoryRepository.delete(priceCategory);

        auditLogService.log(AuditAction.DELETE, "PriceCategory", id,
            priceCategory.getName(), null, getCurrentUsername());
    }

    @Transactional
    public int deactivateExpiredPrices() {
        List<PriceCategory> expired = priceCategoryRepository.findExpiredPrices(LocalDate.now());
        expired.forEach(price -> price.setActive(false));
        priceCategoryRepository.saveAll(expired);
        return expired.size();
    }

    private String getCurrentUsername() {
        return "SYSTEM";
    }
}
