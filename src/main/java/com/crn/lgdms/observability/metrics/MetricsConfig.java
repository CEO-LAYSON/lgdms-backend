package com.crn.lgdms.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    private final ConcurrentHashMap<String, AtomicLong> gaugeMap = new ConcurrentHashMap<>();

    @Bean
    public MeterRegistry meterRegistry(MeterRegistry registry) {
        // Custom metrics will be registered here
        return registry;
    }

    public Timer createTimer(String name, String description, String... tags) {
        return Timer.builder(name)
            .description(description)
            .tags(tags)
            .register(meterRegistry(null));
    }

    public void recordGauge(String name, double value, String... tags) {
        AtomicLong gauge = gaugeMap.computeIfAbsent(name, k -> new AtomicLong());
        gauge.set((long) value);
    }
}
