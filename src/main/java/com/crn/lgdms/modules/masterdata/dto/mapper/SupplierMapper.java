package com.crn.lgdms.modules.masterdata.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.masterdata.domain.entity.Supplier;
import com.crn.lgdms.modules.masterdata.dto.request.CreateSupplierRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdateSupplierRequest;
import com.crn.lgdms.modules.masterdata.dto.response.SupplierResponse;
import org.mapstruct.*;

@Mapper(config = BaseMapperConfig.class)
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Supplier toEntity(CreateSupplierRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateSupplierRequest request, @MappingTarget Supplier supplier);

    SupplierResponse toResponse(Supplier supplier);
}
