package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
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
    // LEGACY (mantido) - NÃO MEXER
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
    // ✅ OPERATION KPI (EDIT/DELETE por FY+Quarter+MonthRef) - MANTIDO
    // ============================================================

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
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

    // ============================================================
    // ✅ VOC – (KPI_RECORD) - mantidos
    // ============================================================

    @Query("""
        SELECT r
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.metric = 'VOC'
        ORDER BY r.id DESC
    """)
    List<KpiRecord> findLast6VocByFactory(@Param("factoryId") Long factoryId,
                                         Pageable pageable);

    default List<KpiRecord> findLast6VocByFactory(Long factoryId) {
        return findLast6VocByFactory(factoryId, org.springframework.data.domain.PageRequest.of(0, 6));
    }

    @Query("""
        SELECT r
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND (r.metric = 'VOC' OR r.type = 'VOC')
        ORDER BY r.id DESC
    """)
    List<KpiRecord> findLastVocAnyFieldByFactory(@Param("factoryId") Long factoryId,
                                                Pageable pageable);

    default List<KpiRecord> findLast6VocAnyByFactory(Long factoryId) {
        return findLastVocAnyFieldByFactory(factoryId, org.springframework.data.domain.PageRequest.of(0, 6));
    }

    // ============================================================
    // ✅ VOC – LAST SIX MONTHS (VOC_RECORD) ✅ CORRIGIDO
    // Tabela VOC_RECORD tem: month_ref e voc (não existe month/voc_value)
    // ============================================================

    // (1) Sem FY/Quarter (fallback geral por fábrica)
    @Query(value = """
        SELECT month_ref, voc
        FROM voc_record
        WHERE factory_id = :factoryId
        ORDER BY created_at DESC
        LIMIT 6
    """, nativeQuery = true)
    List<Object[]> findLast6VocFromVocRecord(@Param("factoryId") Long factoryId);

    // (2) FY + Quarter (principal)
    @Query(value = """
        SELECT month_ref, voc
        FROM voc_record
        WHERE factory_id = :factoryId
          AND fy = :fy
          AND quarter = :quarter
        ORDER BY created_at DESC
        LIMIT 6
    """, nativeQuery = true)
    List<Object[]> findLast6VocFromVocRecord(@Param("factoryId") Long factoryId,
                                            @Param("fy") String fy,
                                            @Param("quarter") String quarter);

    // (3) ✅ NOVO: FY (sem quarter) -> pra VOC funcionar nos 4 quarters (fallback)
    @Query(value = """
        SELECT month_ref, voc
        FROM voc_record
        WHERE factory_id = :factoryId
          AND fy = :fy
        ORDER BY created_at DESC
        LIMIT 6
    """, nativeQuery = true)
    List<Object[]> findLast6VocFromVocRecordByFy(@Param("factoryId") Long factoryId,
                                                @Param("fy") String fy);

    // ============================================================
    // ✅ HFPI ONLINE (%): busca no KPI_RECORD (mantido)
    // ============================================================
    @Query("""
        SELECT AVG(r.kpiValue)
        FROM KpiRecord r
        WHERE r.factory.id = :factoryId
          AND r.fy = :fy
          AND r.quarter = :quarter
          AND (:monthRef IS NULL OR :monthRef = '' OR r.monthRef = :monthRef)
          AND (
                r.type = 'HFPI_ONLINE'
             OR r.metric = 'HFPI_ONLINE'
             OR r.metric = 'HFPI ONLINE'
             OR (r.type = 'HFPI' AND (UPPER(r.metric) LIKE '%ONLINE%'))
          )
    """)
    Double findHfpiOnlineValue(@Param("factoryId") Long factoryId,
                               @Param("fy") String fy,
                               @Param("quarter") String quarter,
                               @Param("monthRef") String monthRef);

}
