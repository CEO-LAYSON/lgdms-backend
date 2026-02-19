package com.crn.lgdms.modules.inventory.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.inventory.domain.entity.StockAdjustment;
import com.crn.lgdms.modules.inventory.dto.request.CreateAdjustmentRequest;
import com.crn.lgdms.modules.inventory.dto.response.AdjustmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface StockAdjustmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adjustmentNumber", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "cylinderSize", ignore = true)
    @Mapping(target = "oldQuantity", ignore = true)
    @Mapping(target = "difference", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    StockAdjustment toEntity(CreateAdjustmentRequest request);

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    AdjustmentResponse toResponse(StockAdjustment adjustment);
}
