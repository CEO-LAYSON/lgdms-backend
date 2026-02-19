package com.crn.lgdms.modules.users.service;

import com.crn.lgdms.common.enums.AuditAction;
import com.crn.lgdms.common.exception.ConflictException;
import com.crn.lgdms.common.exception.NotFoundException;
import com.crn.lgdms.common.exception.ValidationException;
import com.crn.lgdms.common.security.userdetails.SecurityUser;
import com.crn.lgdms.modules.users.domain.entity.Role;
import com.crn.lgdms.modules.users.domain.entity.User;
import com.crn.lgdms.modules.users.domain.valueobject.PasswordPolicy;
import com.crn.lgdms.modules.users.dto.request.CreateUserRequest;
import com.crn.lgdms.modules.users.dto.request.UpdateUserRequest;
import com.crn.lgdms.modules.users.dto.request.AssignRoleRequest;
import com.crn.lgdms.modules.users.dto.response.UserResponse;
import com.crn.lgdms.modules.users.dto.mapper.UserMapper;
import com.crn.lgdms.modules.users.repository.RoleRepository;
import com.crn.lgdms.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PasswordPolicy passwordPolicy;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        // Validate password policy
        if (!passwordPolicy.isValid(request.getPassword())) {
            throw new ValidationException(passwordPolicy.getPolicyDescription());
        }

        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }

        // Check for existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        // Map and save user
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign roles if provided
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            if (roles.size() != request.getRoleIds().size()) {
                throw new NotFoundException("One or more roles not found");
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);

        // Audit log
        auditLogService.log(AuditAction.CREATE, "User", savedUser.getId(),
            null, savedUser.getUsername(), getCurrentUsername());

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable)
            .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);
        return userRepository.searchUsers(searchTerm, pageable)
            .map(userMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Check email uniqueness if changing
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
        }

        User oldUser = userMapper.toResponse(user); // For audit log
        userMapper.updateEntity(request, user);

        User updatedUser = userRepository.save(user);

        // Audit log
        auditLogService.log(AuditAction.UPDATE, "User", id,
            oldUser.toString(), userMapper.toResponse(updatedUser).toString(),
            getCurrentUsername());

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // Soft delete or hard delete? Let's do soft delete by deactivating
        user.setActive(false);
        userRepository.save(user);

        // Audit log
        auditLogService.log(AuditAction.DELETE, "User", id,
            user.getUsername(), null, getCurrentUsername());

        log.info("User deactivated successfully with ID: {}", id);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponse assignRoles(String userId, AssignRoleRequest request) {
        log.info("Assigning roles to user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
        if (roles.size() != request.getRoleIds().size()) {
            throw new NotFoundException("One or more roles not found");
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);

        // Audit log
        auditLogService.log(AuditAction.UPDATE, "UserRoles", userId,
            null, "Roles assigned: " + request.getRoleIds(), getCurrentUsername());

        log.info("Roles assigned successfully to user ID: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidationException("Old password is incorrect");
        }

        // Validate new password
        if (!passwordPolicy.isValid(newPassword)) {
            throw new ValidationException(passwordPolicy.getPolicyDescription());
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Audit log
        auditLogService.log(AuditAction.UPDATE, "UserPassword", userId,
            null, "Password changed", getCurrentUsername());

        log.info("Password changed successfully for user ID: {}", userId);
    }

    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            return ((SecurityUser) authentication.getPrincipal()).getUsername();
        }
        return "SYSTEM";
    }
}
