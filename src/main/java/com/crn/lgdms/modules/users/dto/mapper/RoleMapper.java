package com.crn.lgdms.modules.users.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.users.domain.entity.Role;
import com.crn.lgdms.modules.users.dto.request.CreateRoleRequest;
import com.crn.lgdms.modules.users.dto.request.UpdateRoleRequest;
import com.crn.lgdms.modules.users.dto.response.RoleResponse;
import org.mapstruct.*;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapperConfig.class, uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Role toEntity(CreateRoleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateRoleRequest request, @MappingTarget Role role);

    @Mapping(target = "permissionNames", expression = "java(getPermissionNames(role))")
    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    RoleResponse toResponse(Role role);

    default Set<String> getPermissionNames(Role role) {
        if (role.getPermissions() == null) return Set.of();
        return role.getPermissions().stream()
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
    }
}
