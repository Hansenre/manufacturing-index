package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "voc_record",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"factory_id", "fy", "quarter", "month_ref"}
    )
)
public class VocRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @Column(nullable = false)
    private String fy;        // FY26

    @Column(nullable = false)
    private String quarter;   // Q1

    @Column(name = "month_ref", nullable = false)
    private String monthRef;  // JAN, FEB...

    @Column(nullable = false)
    private Double voc;       // 0.0 – 30.0

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Factory getFactory() {
        return factory;
    }

    public void setFactory(Factory factory) {
        this.factory = factory;
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

    public Double getVoc() {
        return voc;
    }

    public void setVoc(Double voc) {
        this.voc = voc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
