package com.crn.lgdms.observability.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        Instant start = Instant.now();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            Instant finish = Instant.now();
            long duration = Duration.between(start, finish).toMillis();

            // Log only in debug mode or for errors
            if (response.getStatus() >= 400 || log.isDebugEnabled()) {
                logRequestDetails(requestWrapper, responseWrapper, duration);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestDetails(ContentCachingRequestWrapper request,
                                   ContentCachingResponseWrapper response,
                                   long duration) {

        String requestBody = getContentAsString(request.getContentAsByteArray(),
            request.getCharacterEncoding());
        String responseBody = getContentAsString(response.getContentAsByteArray(),
            response.getCharacterEncoding());

        log.debug("Request: {} {} - Params: {} - Body: {}",
            request.getMethod(),
            request.getRequestURI(),
            request.getParameterMap(),
            requestBody);

        if (response.getStatus() >= 400) {
            log.warn("Response: {} - Duration: {}ms - Body: {}",
                response.getStatus(), duration, responseBody);
        } else {
            log.debug("Response: {} - Duration: {}ms - Body: {}",
                response.getStatus(), duration, responseBody);
        }
    }

    private String getContentAsString(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            return new String(content, encoding != null ? encoding : "UTF-8");
        } catch (Exception e) {
            return "[Binary Content]";
        }
    }
}
