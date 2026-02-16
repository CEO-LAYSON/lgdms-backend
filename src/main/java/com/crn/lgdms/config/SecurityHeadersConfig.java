package com.crn.lgdms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.server.header.ContentSecurityPolicyServerHttpHeadersWriter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter>
    securityHeadersFilter() {
        org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> registration =
            new org.springframework.boot.web.servlet.FilterRegistrationBean<>();
        registration.setFilter(new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response,
                                            jakarta.servlet.FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("Content-Security-Policy", "default-src 'self'");
                chain.doFilter(request, response);
            }
        });
        registration.addUrlPatterns("/*");
        return registration;
    }
}
