package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "hfpi_factory",
    indexes = {
        @Index(name = "idx_hfpi_factory_factory", columnList = "factory_id"),
        @Index(name = "idx_hfpi_factory_fy_quarter_month", columnList = "fy, quarter, month_ref")
    }
)
public class HfpiFactory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex.: "FY26"
    @Column(nullable = false)
    private String fy;

    // Ex.: "Q1", "Q2", "Q3", "Q4"
    @Column(nullable = false)
    private String quarter;

    // ⚠️ "month" dá erro no H2. Então a coluna é month_ref.
    // Ex.: "Jun", "Jul", "Aug" ou "2025-06" (como você preferir)
    @Column(name = "month_ref", nullable = false)
    private String month;

    @Column(name = "hfpi_aprovados", nullable = false)
    private Integer hfpiAprovados = 0;

    @Column(name = "hfpi_realizado", nullable = false)
    private Integer hfpiRealizado = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    public HfpiFactory() {}

    // ===== Getters/Setters =====

    public Long getId() { return id; }

    public String getFy() { return fy; }
    public void setFy(String fy) { this.fy = fy; }

    public String getQuarter() { return quarter; }
    public void setQuarter(String quarter) { this.quarter = quarter; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Integer getHfpiAprovados() { return hfpiAprovados; }
    public void setHfpiAprovados(Integer hfpiAprovados) { this.hfpiAprovados = hfpiAprovados; }

    public Integer getHfpiRealizado() { return hfpiRealizado; }
    public void setHfpiRealizado(Integer hfpiRealizado) { this.hfpiRealizado = hfpiRealizado; }

    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }
}
