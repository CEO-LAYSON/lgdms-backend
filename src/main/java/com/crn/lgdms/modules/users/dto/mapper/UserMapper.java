package com.crn.lgdms.modules.users.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.users.domain.entity.User;
import com.crn.lgdms.modules.users.dto.request.CreateUserRequest;
import com.crn.lgdms.modules.users.dto.request.UpdateUserRequest;
import com.crn.lgdms.modules.users.dto.response.UserResponse;
import org.mapstruct.*;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapperConfig.class, uses = {RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roleNames", expression = "java(getRoleNames(user))")
    @Mapping(target = "permissions", expression = "java(getPermissions(user))")
    UserResponse toResponse(User user);

    default Set<String> getRoleNames(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet());
    }

    default Set<String> getPermissions(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
    }
}
