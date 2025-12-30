package com.manufacturing.manufacturingindex.dto;

public class HfpiDashboardDTO {

    private double hfpiOnline;
    private double hfpiFactory;
    private double hfpiFinal;

    public HfpiDashboardDTO(double hfpiOnline, double hfpiFactory, double hfpiFinal) {
        this.hfpiOnline = hfpiOnline;
        this.hfpiFactory = hfpiFactory;
        this.hfpiFinal = hfpiFinal;
    }

    public double getHfpiOnline() {
        return hfpiOnline;
    }

    public double getHfpiFactory() {
        return hfpiFactory;
    }

    public double getHfpiFinal() {
        return hfpiFinal;
    }
}
