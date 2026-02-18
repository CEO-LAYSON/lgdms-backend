package com.crn.lgdms.modules.transfer.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.transfer.domain.entity.TransferRequestItem;
import com.crn.lgdms.modules.transfer.dto.request.AddTransferRequestItemRequest;
import com.crn.lgdms.modules.transfer.dto.response.TransferRequestItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface TransferRequestItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transferRequest", ignore = true)
    @Mapping(target = "cylinderSize", ignore = true)
    @Mapping(target = "approvedQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TransferRequestItem toEntity(AddTransferRequestItemRequest request);

    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    TransferRequestItemResponse toResponse(TransferRequestItem item);
}
