package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_record")
public class WasteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @Column(nullable = false, length = 10)
    private String fy;

    @Column(nullable = false, length = 2)
    private String quarter;

    // ✅ H2 não gosta de "month" como nome de coluna
    @Column(name = "month_ref", length = 3, nullable = false)
    private String month;   // <-- o nome do atributo é month (não monthRef)


    @Column(name = "waste_gr_pairs", nullable = false)
    private Double wasteGrPairs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WasteRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public WasteRecord(Factory factory, String fy, String quarter, String month, Double wasteGrPairs) {
        this.factory = factory;
        this.fy = fy;
        this.quarter = quarter;
        this.month = month;
        this.wasteGrPairs = wasteGrPairs;
        this.createdAt = LocalDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }

    public String getFy() { return fy; }
    public void setFy(String fy) { this.fy = fy; }

    public String getQuarter() { return quarter; }
    public void setQuarter(String quarter) { this.quarter = quarter; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Double getWasteGrPairs() { return wasteGrPairs; }
    public void setWasteGrPairs(Double wasteGrPairs) { this.wasteGrPairs = wasteGrPairs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
