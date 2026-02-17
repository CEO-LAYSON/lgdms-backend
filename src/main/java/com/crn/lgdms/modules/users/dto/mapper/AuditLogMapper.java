package com.crn.lgdms.modules.users.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.users.domain.entity.AuditLog;
import com.crn.lgdms.modules.users.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface AuditLogMapper {

    @Mapping(target = "username", ignore = true) // Will be populated in service
    AuditLogResponse toResponse(AuditLog auditLog);
}
