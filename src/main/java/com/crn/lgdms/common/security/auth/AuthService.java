package com.crn.lgdms.common.security.auth;

import com.crn.lgdms.common.exception.UnauthorizedException;
import com.crn.lgdms.common.security.Jwt.JwtService;
import com.crn.lgdms.common.security.Jwt.TokenBlacklistService;
import com.crn.lgdms.common.security.auth.dto.LoginRequest;
import com.crn.lgdms.common.security.auth.dto.LoginResponse;
import com.crn.lgdms.common.security.auth.dto.RefreshTokenRequest;
import com.crn.lgdms.common.security.auth.dto.RefreshTokenResponse;
import com.crn.lgdms.common.security.userdetails.SecurityUser;
import com.crn.lgdms.modules.users.domain.entity.User;
import com.crn.lgdms.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = userRepository.findByUsername(securityUser.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Update last login
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", securityUser.getId());
        claims.put("email", securityUser.getEmail());
        claims.put("roles", securityUser.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .filter(auth -> auth.startsWith("ROLE_"))
            .collect(Collectors.toList()));

        String accessToken = jwtService.generateToken(claims, securityUser);

        // Build response
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(generateRefreshToken(securityUser))
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .username(securityUser.getUsername())
            .email(securityUser.getEmail())
            .roles(securityUser.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority().substring(5))
                .collect(Collectors.toList()))
            .permissions(securityUser.getAuthorities().stream()
                .filter(auth -> !auth.getAuthority().startsWith("ROLE_"))
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList()))
            .build();
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // Validate refresh token
        String username = jwtService.extractUsername(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        SecurityUser securityUser = new SecurityUser(user);

        // Generate new access token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        String newAccessToken = jwtService.generateToken(claims, securityUser);

        return RefreshTokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(request.getRefreshToken()) // Return same refresh token
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationTime())
            .build();
    }

    public void logout(String token) {
        // Remove Bearer prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Blacklist the token
        if (token != null && !token.isEmpty()) {
            tokenBlacklistService.blacklistToken(token);
            log.info("User logged out, token blacklisted");
        }

        SecurityContextHolder.clearContext();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return userRepository.findByUsername(securityUser.getUsername())
            .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private String generateRefreshToken(SecurityUser securityUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("userId", securityUser.getId());

        // Refresh token with longer expiry
        return jwtService.generateToken(claims, securityUser);
    }
}
