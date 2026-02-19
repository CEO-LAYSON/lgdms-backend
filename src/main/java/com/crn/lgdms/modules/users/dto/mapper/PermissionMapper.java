package com.crn.lgdms.modules.users.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.users.domain.entity.Permission;
import com.crn.lgdms.modules.users.dto.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);
}
