package com.crn.lgdms.modules.locations.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.locations.domain.entity.Location;
import com.crn.lgdms.modules.locations.dto.request.CreateLocationRequest;
import com.crn.lgdms.modules.locations.dto.request.UpdateLocationRequest;
import com.crn.lgdms.modules.locations.dto.response.LocationResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class)
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Location toEntity(CreateLocationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code shouldn't be changed
    @Mapping(target = "locationType", ignore = true) // Type shouldn't be changed
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateLocationRequest request, @MappingTarget Location location);

    LocationResponse toResponse(Location location);
}
