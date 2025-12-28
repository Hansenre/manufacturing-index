package com.manufacturing.manufacturingindex.dto;

public class DefectChartDTO {

    private String defect;
    private String severity;
    private Long count;

    public DefectChartDTO(String defect, String severity, Long count) {
        this.defect = defect;
        this.severity = severity;
        this.count = count;
    }

    public String getDefect() { return defect; }
    public String getSeverity() { return severity; }
    public Long getCount() { return count; }
}

