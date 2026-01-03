package com.manufacturing.manufacturingindex.dto;

public class DrTopTypeDTO {

    // mantém compatibilidade com o que você já tinha
    private String defectType;
    private long total;

    // novo (para o one-pager/view.html usar item.rate)
    private double rate;

    public DrTopTypeDTO() {
    }

    // compatível com o que você já usava
    public DrTopTypeDTO(String defectType, long total) {
        this.defectType = defectType;
        this.total = total;
        this.rate = 0.0;
    }

    // novo construtor completo (recomendado)
    public DrTopTypeDTO(String defectType, long total, double rate) {
        this.defectType = defectType;
        this.total = total;
        this.rate = rate;
    }

    // ===== getters/setters originais =====
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    // ===== ALIASES para o Thymeleaf (item.name e item.rate) =====
    public String getName() {
        return defectType; // item.name
    }

    // item.rate já existe via getRate()
}
