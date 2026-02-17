package com.crn.lgdms.modules.masterdata.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.masterdata.domain.entity.CylinderSize;
import com.crn.lgdms.modules.masterdata.dto.request.CreateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.request.UpdateCylinderSizeRequest;
import com.crn.lgdms.modules.masterdata.dto.response.CylinderSizeResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class)
public interface CylinderSizeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    CylinderSize toEntity(CreateCylinderSizeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateCylinderSizeRequest request, @MappingTarget CylinderSize cylinderSize);

    CylinderSizeResponse toResponse(CylinderSize cylinderSize);
}
