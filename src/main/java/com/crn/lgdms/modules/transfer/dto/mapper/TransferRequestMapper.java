package com.crn.lgdms.modules.transfer.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.transfer.domain.entity.TransferRequest;
import com.crn.lgdms.modules.transfer.dto.request.CreateTransferRequestRequest;
import com.crn.lgdms.modules.transfer.dto.response.TransferRequestResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class, uses = {TransferRequestItemMapper.class})
public interface TransferRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestNumber", ignore = true)
    @Mapping(target = "fromLocation", ignore = true)
    @Mapping(target = "toLocation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TransferRequest toEntity(CreateTransferRequestRequest request);

    @Mapping(target = "fromLocationId", source = "fromLocation.id")
    @Mapping(target = "fromLocationName", source = "fromLocation.name")
    @Mapping(target = "toLocationId", source = "toLocation.id")
    @Mapping(target = "toLocationName", source = "toLocation.name")
    @Mapping(target = "items", source = "items")
    TransferRequestResponse toResponse(TransferRequest request);
}
