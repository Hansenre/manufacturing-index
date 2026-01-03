package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.HfpiFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HfpiFactoryRepository extends JpaRepository<HfpiFactory, Long> {

    // =========================================================
    // LISTAS (para tela HFPI / filtros)
    // =========================================================

    @Query("""
        SELECT h
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
        ORDER BY h.fy DESC, h.quarter DESC, h.month ASC, h.id DESC
    """)
    List<HfpiFactory> findAllByFactory(@Param("factoryId") Long factoryId);

    @Query("""
        SELECT h
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
          AND (:fy IS NULL OR :fy = '' OR h.fy = :fy)
          AND (:quarter IS NULL OR :quarter = '' OR h.quarter = :quarter)
        ORDER BY h.month ASC, h.id DESC
    """)
    List<HfpiFactory> findByFactoryAndFyQuarter(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    @Query("""
        SELECT DISTINCT h.fy
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
        ORDER BY h.fy DESC
    """)
    List<String> listFy(@Param("factoryId") Long factoryId);

    @Query("""
        SELECT DISTINCT h.quarter
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
          AND (:fy IS NULL OR :fy = '' OR h.fy = :fy)
        ORDER BY h.quarter ASC
    """)
    List<String> listQuarter(@Param("factoryId") Long factoryId, @Param("fy") String fy);

    // =========================================================
    // ✅ ONE PAGER (pegar 1 registro certo)
    // =========================================================

    Optional<HfpiFactory> findTopByFactoryIdAndFyAndQuarterOrderByIdDesc(
            Long factoryId, String fy, String quarter
    );

    Optional<HfpiFactory> findTopByFactoryIdAndFyAndQuarterAndMonthOrderByIdDesc(
            Long factoryId, String fy, String quarter, String month
    );

    Optional<HfpiFactory> findTopByFactoryIdOrderByFyDescQuarterDescIdDesc(Long factoryId);

    // =========================================================
    // NATIVE QUERY (percent HFPI Factory) - sem month
    // =========================================================

    @Query(value = """
        SELECT
            COALESCE(SUM(hfpi_aprovados), 0) AS aprovados,
            COALESCE(SUM(hfpi_realizado), 0) AS realizados,
            ROUND(
                COALESCE(SUM(hfpi_aprovados), 0) * 100.0 /
                NULLIF(COALESCE(SUM(hfpi_realizado), 0), 0),
                2
            ) AS hfpi_factory_percent
        FROM hfpi_factory
        WHERE factory_id = :factoryId
          AND fy = :fy
          AND quarter = :quarter
    """, nativeQuery = true)
    Object[] getHfpiFactoryPercent(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    // =========================================================
    // JPQL (por entidade) - soma aprovados/realizados (opcional mês)
    // =========================================================

    @Query("""
        SELECT
            COALESCE(SUM(h.hfpiAprovados), 0),
            COALESCE(SUM(h.hfpiRealizado), 0)
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
          AND h.fy = :fy
          AND h.quarter = :quarter
          AND (:month IS NULL OR :month = '' OR h.month = :month)
    """)
    Object[] sumHfpiApprovedRealized(@Param("factoryId") Long factoryId,
                                     @Param("fy") String fy,
                                     @Param("quarter") String quarter,
                                     @Param("month") String month
    );

    // =========================================================
    // ✅ NATIVE QUERY (percent HFPI Factory) - COM month
    // ✅ CORRIGIDO PARA H2: coluna real é MONTH_
    // =========================================================

    @Query(value = """
        SELECT
            COALESCE(SUM(hfpi_aprovados), 0) AS aprovados,
            COALESCE(SUM(hfpi_realizado), 0) AS realizados,
            ROUND(
                COALESCE(SUM(hfpi_aprovados), 0) * 100.0 /
                NULLIF(COALESCE(SUM(hfpi_realizado), 0), 0),
                2
            ) AS hfpi_factory_percent
        FROM hfpi_factory
        WHERE factory_id = :factoryId
          AND fy = :fy
          AND quarter = :quarter
          AND (:month IS NULL OR :month = '' OR MONTH_ = :month)
    """, nativeQuery = true)
    Object[] getHfpiFactoryPercentWithMonth(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter,
            @Param("month") String month
    );
}
