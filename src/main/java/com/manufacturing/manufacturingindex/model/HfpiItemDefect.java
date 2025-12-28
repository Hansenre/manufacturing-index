package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;

@Entity
public class HfpiItemDefect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private HfpiItem item;

    @ManyToOne
    @JoinColumn(nullable = false)
    private DefectType defectType;

    @Column(nullable = false)
    private String severity; // MENOR | MODERADO | SEVERO

    public Long getId() {
        return id;
    }

    public HfpiItem getItem() {
        return item;
    }

    public void setItem(HfpiItem item) {
        this.item = item;
    }

    public DefectType getDefectType() {
        return defectType;
    }

    public void setDefectType(DefectType defectType) {
        this.defectType = defectType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
