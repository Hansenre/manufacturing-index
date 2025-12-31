package com.manufacturing.manufacturingindex.dto;

public class OperationKpiDTO {

    /* ======================
       PERIOD
    ====================== */

    private String fy;
    private String quarter;
    private String monthRef;

    /* ======================
       KPI VALUES
    ====================== */

    private Integer pairsProduced;
    private Integer workingDays;
    private Integer workforceNike;
    private Double pph;
    private Double dr;

    /* ======================
       GETTERS / SETTERS
    ====================== */

    public String getFy() {
        return fy;
    }

    public void setFy(String fy) {
        this.fy = fy;
    }

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getMonthRef() {
        return monthRef;
    }

    public void setMonthRef(String monthRef) {
        this.monthRef = monthRef;
    }

    public Integer getPairsProduced() {
        return pairsProduced;
    }

    public void setPairsProduced(Integer pairsProduced) {
        this.pairsProduced = pairsProduced;
    }

    public Integer getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }

    public Integer getWorkforceNike() {
        return workforceNike;
    }

    public void setWorkforceNike(Integer workforceNike) {
        this.workforceNike = workforceNike;
    }

    public Double getPph() {
        return pph;
    }

    public void setPph(Double pph) {
        this.pph = pph;
    }

    public Double getDr() {
        return dr;
    }

    public void setDr(Double dr) {
        this.dr = dr;
    }
}
