package com.crn.lgdms.modules.reports.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private String locationId;
    private String locationType;
    private String customerId;
    private String productType;
    private String cylinderSizeId;
    private String reportType; // SUMMARY, DETAILED
    private String groupBy; // DAY, WEEK, MONTH, LOCATION, PRODUCT
}
