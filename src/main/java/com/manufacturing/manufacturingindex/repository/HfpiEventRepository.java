package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.HfpiEvent;

public interface HfpiEventRepository extends JpaRepository<HfpiEvent, Long> {

    /* =====================================================
       USADO PELO HFPI CONTROLLER (LISTA DE EVENTOS)
       ===================================================== */
    List<HfpiEvent> findByFactory(Factory factory);

    /* =====================================================
       🍕 Defeitos por descrição
       ===================================================== */
    @Query("""
        SELECT d.name, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect1 d
        WHERE e.factory.id = :factoryId
        GROUP BY d.name

        UNION ALL
        SELECT d.name, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect2 d
        WHERE e.factory.id = :factoryId
        GROUP BY d.name

        UNION ALL
        SELECT d.name, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect3 d
        WHERE e.factory.id = :factoryId
        GROUP BY d.name
    """)
    List<Object[]> countDefectsByDescription(@Param("factoryId") Long factoryId);

    /* =====================================================
       📊 Defeitos por severidade
       ===================================================== */
    @Query("""
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect1 d
        WHERE e.factory.id = :factoryId AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating

        UNION ALL
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect2 d
        WHERE e.factory.id = :factoryId AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating

        UNION ALL
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect3 d
        WHERE e.factory.id = :factoryId AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating
    """)
    List<Object[]> countDefectsBySeverity(@Param("factoryId") Long factoryId);

    /* =====================================================
       📈 Defeitos por FY + Quarter
       ===================================================== */
    @Query(value = """
        SELECT d.name AS defectName, COUNT(*) AS count
        FROM hfpi_events e
        JOIN hfpi_items i ON i.event_id = e.id
        JOIN defect_types_new d ON d.id = i.defect_1_id
        WHERE e.factory_id = :factoryId
          AND e.fy = :fy
          AND e.quarter = :quarter
        GROUP BY d.name

        UNION ALL
        SELECT d.name, COUNT(*)
        FROM hfpi_events e
        JOIN hfpi_items i ON i.event_id = e.id
        JOIN defect_types_new d ON d.id = i.defect_2_id
        WHERE e.factory_id = :factoryId
          AND e.fy = :fy
          AND e.quarter = :quarter
        GROUP BY d.name

        UNION ALL
        SELECT d.name, COUNT(*)
        FROM hfpi_events e
        JOIN hfpi_items i ON i.event_id = e.id
        JOIN defect_types_new d ON d.id = i.defect_3_id
        WHERE e.factory_id = :factoryId
          AND e.fy = :fy
          AND e.quarter = :quarter
        GROUP BY d.name
    """, nativeQuery = true)
    List<Object[]> countDefectsByFYAndQuarterRaw(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    /* =====================================================
       🔽 FY / Quarter
       ===================================================== */
    @Query("SELECT DISTINCT e.fy FROM HfpiEvent e ORDER BY e.fy DESC")
    List<String> findDistinctFYs();

    @Query("""
        SELECT DISTINCT e.quarter
        FROM HfpiEvent e
        WHERE e.fy = :fy
        ORDER BY e.quarter
    """)
    List<String> findQuartersByFY(@Param("fy") String fy);

    /* =====================================================
       👟 MODELOS DISPONÍVEIS (USADO PELO SELECT)
       ===================================================== */
    @Query(value = """
        SELECT e.model_name AS modelName, COUNT(*) AS count
        FROM hfpi_events e
        WHERE e.factory_id = :factoryId
          AND e.fy = :fy
          AND e.quarter = :quarter
          AND e.model_name IS NOT NULL
        GROUP BY e.model_name
        ORDER BY count DESC
    """, nativeQuery = true)
    List<Object[]> countDefectsByModelRaw(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    /* =====================================================
       📊 PARETO 80/20 – Defeitos por MODELO
       ===================================================== */
    @Query(value = """
        SELECT defect_name, SUM(cnt) AS total_count
        FROM (
            SELECT d.name AS defect_name, COUNT(*) AS cnt
            FROM hfpi_events e
            JOIN hfpi_items i ON i.event_id = e.id
            JOIN defect_types_new d ON d.id = i.defect_1_id
            WHERE e.factory_id = :factoryId
              AND e.fy = :fy
              AND e.quarter = :quarter
              AND e.model_name = :modelName
            GROUP BY d.name

            UNION ALL
            SELECT d.name, COUNT(*)
            FROM hfpi_events e
            JOIN hfpi_items i ON i.event_id = e.id
            JOIN defect_types_new d ON d.id = i.defect_2_id
            WHERE e.factory_id = :factoryId
              AND e.fy = :fy
              AND e.quarter = :quarter
              AND e.model_name = :modelName
            GROUP BY d.name

            UNION ALL
            SELECT d.name, COUNT(*)
            FROM hfpi_events e
            JOIN hfpi_items i ON i.event_id = e.id
            JOIN defect_types_new d ON d.id = i.defect_3_id
            WHERE e.factory_id = :factoryId
              AND e.fy = :fy
              AND e.quarter = :quarter
              AND e.model_name = :modelName
            GROUP BY d.name
        ) t
        GROUP BY defect_name
        ORDER BY total_count DESC
    """, nativeQuery = true)
    List<Object[]> countDefectsParetoByModel(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter,
            @Param("modelName") String modelName
    );
}
