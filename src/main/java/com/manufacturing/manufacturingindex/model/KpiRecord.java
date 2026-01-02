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

    @Column(name = "month_ref", nullable = false)
    private String monthRef;

    @Column(nullable = false)
    private String type;   // PAIRS_PRODUCED, WORKING_DAYS, PPH...

    @Column(name = "kpi_value", nullable = false)
    private double kpiValue;

    @Column(nullable = false)
    private double points;

    @ManyToOne
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;
    @Column(nullable = false)
    private String metric;


    // =====================
    // CONSTRUTORES
    // =====================

    public KpiRecord() {
    }

    public KpiRecord(
            String fy,
            String quarter,
            String monthRef,
            String type,
            double kpiValue,
            double points,
            Factory factory
    ) {
        this.fy = fy;
        this.quarter = quarter;
        this.monthRef = monthRef;
        this.type = type;
        this.kpiValue = kpiValue;
        this.points = points;
        this.factory = factory;
    }

    // =====================
    // GETTERS / SETTERS
    // =====================

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

    public String getMonthRef() {
        return monthRef;
    }

    public void setMonthRef(String monthRef) {
        this.monthRef = monthRef;
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

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}
    
    
}
