package com.crn.lgdms.modules.transfer.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.transfer.domain.entity.TransferItem;
import com.crn.lgdms.modules.transfer.dto.response.TransferItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface TransferItemMapper {

    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    @Mapping(target = "outgoingStockLedgerId", source = "outgoingStockLedger.id")
    @Mapping(target = "incomingStockLedgerId", source = "incomingStockLedger.id")
    @Mapping(target = "emptyLedgerId", source = "emptyLedger.id")
    TransferItemResponse toResponse(TransferItem item);
}
