package com.crn.lgdms.modules.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String id;
    private String name;
    private String description;
    private boolean isSystemRole;
    private int userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> permissionNames;
}
