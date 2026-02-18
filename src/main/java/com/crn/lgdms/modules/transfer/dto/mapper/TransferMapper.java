package com.crn.lgdms.modules.transfer.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.transfer.domain.entity.Transfer;
import com.crn.lgdms.modules.transfer.dto.response.TransferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = {TransferItemMapper.class})
public interface TransferMapper {

    @Mapping(target = "fromLocationId", source = "fromLocation.id")
    @Mapping(target = "fromLocationName", source = "fromLocation.name")
    @Mapping(target = "toLocationId", source = "toLocation.id")
    @Mapping(target = "toLocationName", source = "toLocation.name")
    @Mapping(target = "transferRequestId", source = "transferRequest.id")
    @Mapping(target = "transferRequestNumber", source = "transferRequest.requestNumber")
    @Mapping(target = "items", source = "items")
    TransferResponse toResponse(Transfer transfer);
}
