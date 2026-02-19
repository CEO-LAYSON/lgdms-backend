package com.crn.lgdms.common.mapping;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Base configuration for MapStruct mappers in the application.
 * Provides common settings for all mappers including component model,
 * injection strategy, and unmapped policy settings.
 */
@MapperConfig(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.WARN
)
public interface BaseMapperConfig {
}
