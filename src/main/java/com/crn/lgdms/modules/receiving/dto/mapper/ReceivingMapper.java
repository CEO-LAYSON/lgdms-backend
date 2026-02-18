package com.crn.lgdms.modules.receiving.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.receiving.domain.entity.GoodsReceiving;
import com.crn.lgdms.modules.receiving.dto.request.CreateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.request.UpdateReceivingRequest;
import com.crn.lgdms.modules.receiving.dto.response.ReceivingResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class, uses = {ReceivingItemMapper.class})
public interface ReceivingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receivingNumber", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "receivedBy", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    GoodsReceiving toEntity(CreateReceivingRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receivingNumber", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateReceivingRequest request, @MappingTarget GoodsReceiving receiving);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "items", source = "items")
    ReceivingResponse toResponse(GoodsReceiving receiving);
}
