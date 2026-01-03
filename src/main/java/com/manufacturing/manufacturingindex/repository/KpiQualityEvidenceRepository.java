package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.KpiQualityEvidence;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence.MetricKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface KpiQualityEvidenceRepository extends JpaRepository<KpiQualityEvidence, Long> {

    Optional<KpiQualityEvidence> findByFactory_IdAndFyAndQuarterAndMonthAndMetricKey(
            Long factoryId, String fy, String quarter, Integer month, MetricKey metricKey
    );

    List<KpiQualityEvidence> findByFactory_IdAndFyAndQuarterAndMonth(
            Long factoryId, String fy, String quarter, Integer month
    );
}
