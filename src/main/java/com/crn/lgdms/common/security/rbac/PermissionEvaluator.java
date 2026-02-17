package com.crn.lgdms.common.security.rbac;

import com.crn.lgdms.common.security.userdetails.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PermissionEvaluator {

    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null) {
            return false;
        }

        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(permission));
    }

    public boolean hasAnyPermission(Authentication authentication, String... permissions) {
        if (authentication == null) {
            return false;
        }

        SecurityUser user = (SecurityUser) authentication.getPrincipal();

        for (String permission : permissions) {
            if (user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(permission))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }

        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        String roleWithPrefix = "ROLE_" + role;

        return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(roleWithPrefix));
    }
}
