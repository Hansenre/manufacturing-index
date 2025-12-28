package com.manufacturing.manufacturingindex.dto;

public class DefectChartDTO {

    private String defectName;
    private Long quantity;

    public DefectChartDTO(String defectName, Long quantity) {
        this.defectName = defectName;
        this.quantity = quantity;
    }

    public String getDefectName() {
        return defectName;
    }

    public Long getQuantity() {
        return quantity;
    }
}
