package com.crn.lgdms.modules.users.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.users.domain.entity.Permission;
import com.crn.lgdms.modules.users.dto.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}
