package com.crn.lgdms.modules.masterdata.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.masterdata.domain.entity.PriceCategory;
import com.crn.lgdms.modules.masterdata.dto.request.CreatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdatePriceCategoryRequest;
import com.crn.lgdms.modules.masterdata.dto.response.PriceCategoryResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class, uses = {CylinderSizeMapper.class})
public interface PriceCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cylinderSize", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    PriceCategory toEntity(CreatePriceCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cylinderSize", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdatePriceCategoryRequest request, @MappingTarget PriceCategory priceCategory);

    @Mapping(target = "cylinderSizeId", source = "cylinderSize.id")
    @Mapping(target = "cylinderSizeName", source = "cylinderSize.name")
    PriceCategoryResponse toResponse(PriceCategory priceCategory);
}
