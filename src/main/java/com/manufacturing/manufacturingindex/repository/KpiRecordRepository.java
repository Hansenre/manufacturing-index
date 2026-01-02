package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;

public interface KpiRecordRepository extends JpaRepository<KpiRecord, Long> {

    // =========================
    // BÁSICOS (mantidos)
    // =========================
    List<KpiRecord> findByFactoryId(Long factoryId);
    long countByFactoryId(Long factoryId);

    List<KpiRecord> findByFactoryAndFyAndQuarter(Factory factory, String fy, String quarter);

    // ✅ se teu campo na entidade é monthRef, o método TEM que ser MonthRef
    List<KpiRecord> findByFactoryAndFyAndQuarterAndMonthRef(
            Factory factory,
            String fy,
            String quarter,
            String monthRef
    );

    // =========================
    // ONE PAGER – AGREGADOS (Quarter)
    // =========================
    @Query("""
        SELECT COALESCE(SUM(r.kpiValue),0)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.metric = :metric
          AND r.fy = :fy
          AND r.quarter = :quarter
    """)
    Double sumMetric(@Param("factoryId") Long factoryId,
                     @Param("metric") String metric,
                     @Param("fy") String fy,
                     @Param("quarter") String quarter);

    @Query("""
        SELECT COALESCE(AVG(r.kpiValue),0)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.metric = :metric
          AND r.fy = :fy
          AND r.quarter = :quarter
    """)
    Double avgMetric(@Param("factoryId") Long factoryId,
                     @Param("metric") String metric,
                     @Param("fy") String fy,
                     @Param("quarter") String quarter);

    // =========================
    // ✅ ONE PAGER – AGREGADOS (Month opcional)
    // =========================
    @Query("""
        SELECT COALESCE(SUM(r.kpiValue),0)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.metric = :metric
          AND r.fy = :fy
          AND r.quarter = :quarter
          AND (:monthRef IS NULL OR :monthRef = '' OR r.monthRef = :monthRef)
    """)
    Double sumMetricWithMonth(@Param("factoryId") Long factoryId,
                              @Param("metric") String metric,
                              @Param("fy") String fy,
                              @Param("quarter") String quarter,
                              @Param("monthRef") String monthRef);

    @Query("""
        SELECT COALESCE(AVG(r.kpiValue),0)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.metric = :metric
          AND r.fy = :fy
          AND r.quarter = :quarter
          AND (:monthRef IS NULL OR :monthRef = '' OR r.monthRef = :monthRef)
    """)
    Double avgMetricWithMonth(@Param("factoryId") Long factoryId,
                              @Param("metric") String metric,
                              @Param("fy") String fy,
                              @Param("quarter") String quarter,
                              @Param("monthRef") String monthRef);

    // =========================
    // LEGACY (mantido)
    // =========================
    @Query("""
        SELECT
            (SELECT AVG(b.kpiValue)
             FROM KpiRecord b
             WHERE b.factory.id = :factoryId AND b.type = 'BTP'),

            (SELECT AVG(w.kpiValue)
             FROM KpiRecord w
             WHERE w.factory.id = :factoryId),

            (SELECT SUM(w2.kpiValue)
             FROM KpiRecord w2
             WHERE w2.factory.id = :factoryId),

            (SELECT AVG(m.kpiValue)
             FROM KpiRecord m
             WHERE m.factory.id = :factoryId AND m.type = 'MQAAS'),

            (SELECT AVG(d.kpiValue)
             FROM KpiRecord d
             WHERE d.factory.id = :factoryId AND d.type = 'DEFECT')
    """)
    Object[] calculateOperationKpi(@Param("factoryId") Long factoryId);
}
