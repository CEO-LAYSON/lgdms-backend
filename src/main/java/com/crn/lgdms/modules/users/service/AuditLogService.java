package com.crn.lgdms.modules.users.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.modules.users.domain.entity.AuditLog;
import com.crn.lgdms.modules.users.dto.request.AuditLogSearchRequest;
import com.crn.lgdms.modules.users.dto.response.AuditLogResponse;
import com.crn.lgdms.modules.users.dto.mapper.AuditLogMapper;
import com.crn.lgdms.modules.users.repository.AuditLogRepository;
import com.crn.lgdms.modules.users.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void log(AuditAction action, String entityType, String entityId,
                    Object oldValue, Object newValue, String username) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(convertToString(oldValue))
                .newValue(convertToString(newValue))
                .userId(getUserIdFromUsername(username))
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchAuditLogs(AuditLogSearchRequest request, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(
            request.getUserId(),
            request.getAction(),
            request.getEntityType(),
            request.getStartDate(),
            request.getEndDate(),
            pageable
        ).map(auditLog -> {
            AuditLogResponse response = auditLogMapper.toResponse(auditLog);
            // Enrich with username
            if (auditLog.getUserId() != null) {
                userRepository.findById(auditLog.getUserId())
                    .ifPresent(user -> response.setUsername(user.getUsername()));
            }
            return response;
        });
    }

    private String convertToString(Object value) {
        if (value == null) return null;
        try {
            if (value instanceof String) return (String) value;
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    private String getUserIdFromUsername(String username) {
        return userRepository.findByUsername(username)
            .map(user -> user.getId())
            .orElse(null);
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getUserAgent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        HttpServletRequest request = attributes.getRequest();
        return request.getHeader("User-Agent");
    }
}
