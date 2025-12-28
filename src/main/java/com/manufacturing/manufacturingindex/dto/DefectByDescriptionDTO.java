package com.manufacturing.manufacturingindex.dto;

public class DefectByDescriptionDTO {

    private String defectName;
    private Long total;

    public DefectByDescriptionDTO(String defectName, Long total) {
        this.defectName = defectName;
        this.total = total;
    }

    public String getDefectName() {
        return defectName;
    }

    public Long getTotal() {
        return total;
    }
}
