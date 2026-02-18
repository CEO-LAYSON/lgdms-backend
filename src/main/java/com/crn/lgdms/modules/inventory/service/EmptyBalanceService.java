package com.crn.lgdms.modules.inventory.service;

import com.crn.lgdms.common.enums.MovementType;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import com.crn.lgdms.modules.inventory.dto.response.EmptyBalanceResponse;
import com.crn.lgdms.modules.inventory.dto.mapper.EmptyLedgerMapper;
import com.crn.lgdms.modules.inventory.repository.EmptyLedgerRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmptyBalanceService {

    private final EmptyLedgerRepository emptyLedgerRepository;
    private final LocationRepository locationRepository;
    private final CylinderSizeRepository cylinderSizeRepository;
    private final EmptyLedgerMapper emptyLedgerMapper;

    /**
     * Calculate empty balance using the formula:
     * Empty Balance = Empties Issued - Empties Returned
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "emptyBalance", key = "#locationId + '-' + #cylinderSizeId")
    public EmptyBalanceResponse calculateEmptyBalance(String locationId, String cylinderSizeId) {
        log.debug("Calculating empty balance for location: {}, cylinder: {}", locationId, cylinderSizeId);

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));

        CylinderSize cylinderSize = cylinderSizeRepository.findById(cylinderSizeId)
            .orElseThrow(() -> new NotFoundException("Cylinder size not found: " + cylinderSizeId));

        // Get current balance from ledger
        Integer currentBalance = emptyLedgerRepository.getCurrentEmptyBalance(locationId, cylinderSizeId);
        if (currentBalance == null) currentBalance = 0;

        // Calculate expected balance based on transactions
        Integer expectedBalance = calculateExpectedBalance(locationId, cylinderSizeId);

        // Calculate variance
        Integer variance = currentBalance - expectedBalance;

        return EmptyBalanceResponse.builder()
            .locationId(locationId)
            .locationName(location.getName())
            .cylinderSizeId(cylinderSizeId)
            .cylinderSizeName(cylinderSize.getName())
            .currentBalance(currentBalance)
            .expectedBalance(expectedBalance)
            .variance(variance)
            .varianceStatus(emptyLedgerMapper.getVarianceStatus(variance))
            .lastCalculated(LocalDateTime.now())
            .build();
    }

    @Transactional(readOnly = true)
    public List<EmptyBalanceResponse> getAllEmptyBalances(String locationId) {
        log.debug("Getting all empty balances for location: {}", locationId);

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException("Location not found: " + locationId));

        List<CylinderSize> cylinderSizes = cylinderSizeRepository.findByIsActiveTrue();
        List<EmptyBalanceResponse> responses = new ArrayList<>();

        for (CylinderSize cylinderSize : cylinderSizes) {
            responses.add(calculateEmptyBalance(locationId, cylinderSize.getId()));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<EmptyBalanceResponse> getLocationsWithVariance() {
        log.debug("Finding locations with empty cylinder variance");

        List<Location> locations = locationRepository.findAll();
        List<EmptyBalanceResponse> variances = new ArrayList<>();

        for (Location location : locations) {
            List<EmptyBalanceResponse> locationBalances = getAllEmptyBalances(location.getId());
            for (EmptyBalanceResponse balance : locationBalances) {
                if (balance.getVariance() != 0) {
                    variances.add(balance);
                }
            }
        }

        return variances;
    }

    /**
     * Calculate expected empty balance based on transaction history
     * Formula: Empties Issued (from transfers out) - Empties Returned (from transfers in and sales)
     */
    private Integer calculateExpectedBalance(String locationId, String cylinderSizeId) {
        // This would need custom queries to sum by movement type
        // For now, return current balance
        // In production, implement:
        // - Sum of TRANSFER_OUT (empties issued)
        // - Sum of TRANSFER_IN and SALE (empties returned)
        return emptyLedgerRepository.getCurrentEmptyBalance(locationId, cylinderSizeId);
    }
}
