package com.crn.lgdms.modules.inventory.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.inventory.domain.entity.StockSnapshot;
import com.crn.lgdms.modules.inventory.dto.response.ReconciliationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface StockSnapshotMapper {

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    ReconciliationResponse.ReconciliationEntryResponse toEntryResponse(StockSnapshot snapshot);
}
