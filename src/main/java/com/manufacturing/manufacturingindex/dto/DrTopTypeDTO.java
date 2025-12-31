package com.manufacturing.manufacturingindex.dto;

public class DrTopTypeDTO {
    private String defectType;
    private long total;

    public DrTopTypeDTO(String defectType, long total) {
        this.defectType = defectType;
        this.total = total;
    }

	public String getDefectType() {
		return defectType;
	}

	public void setDefectType(String defectType) {
		this.defectType = defectType;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}
    
    
}

