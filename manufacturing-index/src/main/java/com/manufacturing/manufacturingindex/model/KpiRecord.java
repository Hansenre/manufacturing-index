package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;

@Entity
@Table(name = "KPI_RECORD")
public class KpiRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fy;

    @Column(nullable = false)
    private String quarter;

    @Column(nullable = false)
    private String type;   // MQAAS, BTP, DEFECT

    @Column(name = "kpi_value", nullable = false)
    private double kpiValue;

    @Column(nullable = false)
    private double points;

    @ManyToOne
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    // 🔹 Construtor padrão (obrigatório para JPA)
    public KpiRecord() {
    }

    // 🔹 Construtor completo
    public KpiRecord(
            String fy,
            String quarter,
            String type,
            double kpiValue,
            double points,
            Factory factory
    ) {
        this.fy = fy;
        this.quarter = quarter;
        this.type = type;
        this.kpiValue = kpiValue;
        this.points = points;
        this.factory = factory;
    }

    // 🔹 Getters e Setters
    public Long getId() {
        return id;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getKpiValue() {
        return kpiValue;
    }

    public void setKpiValue(double kpiValue) {
        this.kpiValue = kpiValue;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
    }
}
