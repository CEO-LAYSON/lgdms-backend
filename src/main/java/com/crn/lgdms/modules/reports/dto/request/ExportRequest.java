package com.crn.lgdms.modules.reports.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    private String format; // PDF, EXCEL, CSV
    private String reportType;
    private ReportFilterRequest filters;
    private boolean includeCharts;
    private String paperSize; // A4, LETTER
    private String orientation; // PORTRAIT, LANDSCAPE
}
