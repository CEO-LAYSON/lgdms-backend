package com.crn.lgdms.modules.sales.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.sales.domain.entity.SaleItem;
import com.crn.lgdms.modules.sales.dto.response.SaleItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface SaleItemMapper {

    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    @Mapping(target = "stockLedgerId", source = "stockLedger.id")
    @Mapping(target = "emptyLedgerId", source = "emptyLedger.id")
    SaleItemResponse toResponse(SaleItem item);
}
