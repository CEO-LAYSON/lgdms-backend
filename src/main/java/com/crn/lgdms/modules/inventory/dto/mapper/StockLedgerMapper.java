package com.crn.lgdms.modules.inventory.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.inventory.domain.entity.StockLedger;
import com.crn.lgdms.modules.inventory.dto.response.OnHandResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface StockLedgerMapper {

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    OnHandResponse toOnHandResponse(StockLedger ledger);

    default boolean isLowStock(Integer quantity) {
        return quantity != null && quantity < 10;
    }

    default boolean isOutOfStock(Integer quantity) {
        return quantity != null && quantity == 0;
    }
}
