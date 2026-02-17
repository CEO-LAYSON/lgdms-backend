package com.crn.lgdms.common.security.rbac;

import org.springframework.stereotype.Component;

@Component("security")
public class SecurityExpressions {

    private final PermissionEvaluator permissionEvaluator;

    public SecurityExpressions(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    public boolean hasPermission(String permission) {
        return permissionEvaluator.hasPermission(
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),
            permission
        );
    }

    public boolean hasAnyPermission(String... permissions) {
        return permissionEvaluator.hasAnyPermission(
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),
            permissions
        );
    }

    public boolean hasRole(String role) {
        return permissionEvaluator.hasRole(
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),
            role
        );
    }
}
