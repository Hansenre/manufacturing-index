package com.manufacturing.manufacturingindex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_quality_evidence",
uniqueConstraints = @UniqueConstraint(
		   columnNames = {"factory_id", "fy", "quarter", "month", "metric_key"}
		))
public class KpiQualityEvidence {

    public enum MetricKey {
        MPPA, MQAA, BTP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @Column(nullable = false, length = 10)
    private String fy;        // ex: FY25

    @Column(nullable = false, length = 10)
    private String quarter;   // ex: Q1

    @Column(name = "month_num", nullable = false)
    private Integer month;


    @Enumerated(EnumType.STRING)
    @Column(name = "metric_key", nullable = false, length = 10)
    private MetricKey metricKey;

    @Column(nullable = false, length = 255)
    private String fileName;  // nome original

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false, length = 500)
    private String storagePath; // caminho salvo no disco

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // getters/setters
    public Long getId() { return id; }

    public Factory getFactory() { return factory; }
    public void setFactory(Factory factory) { this.factory = factory; }

    public String getFy() { return fy; }
    public void setFy(String fy) { this.fy = fy; }

    public String getQuarter() { return quarter; }
    public void setQuarter(String quarter) { this.quarter = quarter; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public MetricKey getMetricKey() { return metricKey; }
    public void setMetricKey(MetricKey metricKey) { this.metricKey = metricKey; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
