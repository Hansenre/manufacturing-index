package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    // ============================================================
    // ✅ NOVO: OPERATION KPI (EDIT/DELETE por FY+Quarter+MonthRef)
    // ============================================================

    /**
     * Carrega o "conjunto" OPERATION do mês (5 linhas: PAIRS, WORKING_DAYS, WORKFORCE, PPH, DR)
     */
    @Query("""
        SELECT r
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.type = 'OPERATION'
          AND r.fy = :fy
          AND r.quarter = :quarter
          AND r.monthRef = :monthRef
    """)
    List<KpiRecord> findOperationSet(@Param("factoryId") Long factoryId,
                                     @Param("fy") String fy,
                                     @Param("quarter") String quarter,
                                     @Param("monthRef") String monthRef);

    /**
     * Deleta o "conjunto" OPERATION do mês inteiro (todas as métricas OPERATION daquele monthRef)
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.type = 'OPERATION'
          AND r.fy = :fy
          AND r.quarter = :quarter
          AND r.monthRef = :monthRef
    """)
    int deleteOperationSet(@Param("factoryId") Long factoryId,
                           @Param("fy") String fy,
                           @Param("quarter") String quarter,
                           @Param("monthRef") String monthRef);

    /**
     * Lista para a tabela do Operation KPI (uma linha por FY+Quarter+MonthRef),
     * agregando as métricas em colunas.
     *
     * Retorno (Object[]): [0]=fy, [1]=quarter, [2]=monthRef, [3]=pairs, [4]=workingDays,
     *                      [5]=workforce(avg), [6]=pph(avg), [7]=dr(avg)
     */
    @Query("""
        SELECT r.fy, r.quarter, r.monthRef,
               COALESCE(SUM(CASE WHEN r.metric = 'PAIRS_PRODUCED' THEN r.kpiValue ELSE 0 END), 0),
               COALESCE(SUM(CASE WHEN r.metric = 'WORKING_DAYS' THEN r.kpiValue ELSE 0 END), 0),
               COALESCE(AVG(CASE WHEN r.metric = 'WORKFORCE_NIKE' THEN r.kpiValue ELSE NULL END), 0),
               COALESCE(AVG(CASE WHEN r.metric = 'PPH' THEN r.kpiValue ELSE NULL END), 0),
               COALESCE(AVG(CASE WHEN r.metric = 'DR' THEN r.kpiValue ELSE NULL END), 0)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.type = 'OPERATION'
        GROUP BY r.fy, r.quarter, r.monthRef
        ORDER BY r.fy DESC, r.quarter DESC, r.monthRef ASC
    """)
    List<Object[]> listOperationTable(@Param("factoryId") Long factoryId);
}
