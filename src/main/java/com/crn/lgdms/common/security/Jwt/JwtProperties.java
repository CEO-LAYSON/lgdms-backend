package com.crn.lgdms.common.security.Jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expiration = 86400000; // 24 hours default
    private String issuer = "lgdms";
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
