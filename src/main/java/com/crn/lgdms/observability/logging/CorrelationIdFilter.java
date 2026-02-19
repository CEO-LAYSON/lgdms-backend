package com.crn.lgdms.observability.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_LOG = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }

        // Add to MDC for logging
        MDC.put(CORRELATION_ID_LOG, correlationId);

        // Add to response headers
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            log.info("Request completed: {} {} - Status: {}",
                request.getMethod(), request.getRequestURI(), response.getStatus());
        } finally {
            MDC.remove(CORRELATION_ID_LOG);
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
