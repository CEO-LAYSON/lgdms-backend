package com.crn.lgdms.common.mapping;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String asString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public LocalDate asLocalDate(String date) {
        return date != null ? LocalDate.parse(date, DATE_FORMATTER) : null;
    }

    public String asString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public LocalDateTime asLocalDateTime(String dateTime) {
        return dateTime != null ? LocalDateTime.parse(dateTime, DATETIME_FORMATTER) : null;
    }
}
