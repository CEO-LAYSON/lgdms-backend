package com.crn.lgdms.modules.receiving.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.receiving.domain.entity.ReceivingItem;
import com.crn.lgdms.modules.receiving.dto.request.AddReceivingItemRequest;
import com.crn.lgdms.modules.receiving.dto.response.ReceivingItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseMapperConfig.class)
public interface ReceivingItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goodsReceiving", ignore = true)
    @Mapping(target = "cylinderSize", ignore = true)
    @Mapping(target = "stockLedger", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    ReceivingItem toEntity(AddReceivingItemRequest request);

    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    @Mapping(target = "stockLedgerId", source = "stockLedger.id")
    ReceivingItemResponse toResponse(ReceivingItem item);
}
