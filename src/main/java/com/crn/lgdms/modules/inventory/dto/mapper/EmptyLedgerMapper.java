package com.crn.lgdms.modules.inventory.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.inventory.domain.entity.EmptyLedger;
import com.crn.lgdms.modules.inventory.dto.response.EmptyBalanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface EmptyLedgerMapper {

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    @Mapping(target = "currentBalance", source = "runningBalance")
    EmptyBalanceResponse toEmptyBalanceResponse(EmptyLedger ledger);

    default String getVarianceStatus(Integer variance) {
        if (variance == null || variance == 0) return "NORMAL";
        if (Math.abs(variance) <= 5) return "WARNING";
        return "CRITICAL";
    }
}
