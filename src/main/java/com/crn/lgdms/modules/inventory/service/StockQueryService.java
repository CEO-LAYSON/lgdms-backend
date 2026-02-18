package com.crn.lgdms.modules.inventory.service;

import com.crn.lgdms.common.enums.ProductType;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.dto.response.OnHandResponse;
import com.crn.lgdms.modules.inventory.dto.mapper.StockLedgerMapper;
import com.crn.lgdms.modules.inventory.repository.StockLedgerRepository;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.repository.LocationRepository;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.repository.CylinderSizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockQueryService {

    private final StockLedgerRepository stockLedgerRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final StockLedgerMapper stockLedgerMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "onHandStock", key = "#locationId")
    public List<OnHandResponse> getOnHandStock(String locationId) {
        log.debug("Calculating on-hand stock for location: {}", locationId);

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));

        List<CylinderSize> cylinderSizes = cylinderSizeRepository.findByIsActiveTrue();
        List<OnHandResponse> responses = new ArrayList<>();

        for (CylinderSize cylinderSize : cylinderSizes) {
            for (ProductType productType : ProductType.values()) {
                Integer quantity = getCurrentStock(locationId, cylinderSize.getId(), productType);

                OnHandResponse response = OnHandResponse.builder()
                    .locationId(locationId)
                    .locationName(location.getName())
                    .cylinderSizeId(cylinderSize.getId())
                    .cylinderSizeName(cylinderSize.getName())
                    .productType(productType)
                    .quantity(quantity != null ? quantity : 0)
                    .lastUpdated(LocalDateTime.now())
                    .isLowStock(isLowStock(quantity))
                    .isOutOfStock(quantity == null || quantity == 0)
                    .reorderPoint(10) // Configurable
                    .build();

                responses.add(response);
            }
        }

        return responses;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "currentStock", key = "#locationId + '-' + #cylinderSizeId + '-' + #productType")
    public Integer getCurrentStock(String locationId, String cylinderSizeId, ProductType productType) {
        log.debug("Getting current stock for location: {}, cylinder: {}, type: {}",
            locationId, cylinderSizeId, productType);

        return stockLedgerRepository.getCurrentStock(locationId, cylinderSizeId, productType);
    }

    @Transactional(readOnly = true)
    public List<OnHandResponse> getLowStockItems() {
        log.debug("Fetching all low stock items");

        List<Location> locations = locationRepository.findAll();
        List<OnHandResponse> lowStockItems = new ArrayList<>();

        for (Location location : locations) {
            List<OnHandResponse> locationStock = getOnHandStock(location.getId());
            List<OnHandResponse> lowStock = locationStock.stream()
                .filter(OnHandResponse::isLowStock)
                .collect(Collectors.toList());
            lowStockItems.addAll(lowStock);
        }

        return lowStockItems;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "totalStockValue")
    public Double getTotalStockValue() {
        log.debug("Calculating total stock value");

        List<Location> locations = locationRepository.findAll();
        double totalValue = 0.0;

        for (Location location : locations) {
            List<OnHandResponse> stock = getOnHandStock(location.getId());
            for (OnHandResponse item : stock) {
                // In real implementation, would use actual cost from receiving
                double unitValue = getUnitValue(item.getCylinderSizeName(), item.getProductType());
                totalValue += item.getQuantity() * unitValue;
            }
        }

        return totalValue;
    }

    private boolean isLowStock(Integer quantity) {
        return quantity != null && quantity < 10 && quantity > 0;
    }

    private double getUnitValue(String cylinderSizeName, ProductType productType) {
        // Simplified - in real implementation, would get from price categories
        return switch(cylinderSizeName) {
            case "3kg" -> productType == ProductType.COMPLETE ? 35000 : 25000;
            case "6kg" -> productType == ProductType.COMPLETE ? 65000 : 50000;
            case "15kg" -> productType == ProductType.COMPLETE ? 150000 : 120000;
            case "38kg" -> productType == ProductType.COMPLETE ? 350000 : 280000;
            case "100kg" -> productType == ProductType.COMPLETE ? 850000 : 700000;
            default -> 50000;
        };
    }
}
