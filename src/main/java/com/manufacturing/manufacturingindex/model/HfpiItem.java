package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hfpi_items")
public class HfpiItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer itemNumber;

    @Column(nullable = false)
    private String rating; // BOM / MENOR / MODERADO / SEVERO

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private HfpiEvent event;

    // Defeito 1
    @ManyToOne
    @JoinColumn(name = "defect_1_id")
    private DefectType defect1;

    // Defeito 2
    @ManyToOne
    @JoinColumn(name = "defect_2_id")
    private DefectType defect2;

    // Defeito 3
    @ManyToOne
    @JoinColumn(name = "defect_3_id")
    private DefectType defect3;

    // =====================
    // GETTERS & SETTERS
    // =====================

    public Long getId() {
        return id;
    }

    public Integer getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(Integer itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public HfpiEvent getEvent() {
        return event;
    }

    public void setEvent(HfpiEvent event) {
        this.event = event;
    }

    public DefectType getDefect1() {
        return defect1;
    }

    public void setDefect1(DefectType defect1) {
        this.defect1 = defect1;
    }

    public DefectType getDefect2() {
        return defect2;
    }

    public void setDefect2(DefectType defect2) {
        this.defect2 = defect2;
    }

    public DefectType getDefect3() {
        return defect3;
    }

    public void setDefect3(DefectType defect3) {
        this.defect3 = defect3;
    }
}
