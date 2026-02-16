package com.crn.lgdms.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bandwidth limit() {
        return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
    }
}
