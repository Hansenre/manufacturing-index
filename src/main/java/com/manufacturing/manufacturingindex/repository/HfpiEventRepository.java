package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.HfpiEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HfpiEventRepository extends JpaRepository<HfpiEvent, Long> {

    /* =====================================================
       USADO PELO HFPI CONTROLLER (LISTA DE EVENTOS)
       ===================================================== */
    List<HfpiEvent> findByFactory(Factory factory);

    /* =====================================================
       🍕 Defeitos por descrição (Pizza / Barras antigas)
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
       📈 Defeitos por FY + Quarter (Dashboard dinâmico)
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

        SELECT d.name AS defectName, COUNT(*) AS count
        FROM hfpi_events e
        JOIN hfpi_items i ON i.event_id = e.id
        JOIN defect_types_new d ON d.id = i.defect_2_id
        WHERE e.factory_id = :factoryId
          AND e.fy = :fy
          AND e.quarter = :quarter
        GROUP BY d.name

        UNION ALL

        SELECT d.name AS defectName, COUNT(*) AS count
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
       🔽 FILTROS DINÂMICOS (FY / Quarter)
       ===================================================== */

    // Lista FYs existentes no banco
    @Query("""
        SELECT DISTINCT e.fy
        FROM HfpiEvent e
        ORDER BY e.fy DESC
    """)
    List<String> findDistinctFYs();

    // Lista Quarters por FY
    @Query("""
        SELECT DISTINCT e.quarter
        FROM HfpiEvent e
        WHERE e.fy = :fy
        ORDER BY e.quarter
    """)
    List<String> findQuartersByFY(@Param("fy") String fy);
    
    /* =====================================================
    📦 Defeitos por Modelo (modelName)
    ===================================================== */
    /* =====================================================
    👟 Defeitos por Modelo (CORRIGIDO)
    ===================================================== */
 @Query(value = """
     SELECT e.model_name AS modelName, COUNT(d.id) AS count
     FROM hfpi_events e
     JOIN hfpi_items i ON i.event_id = e.id
     JOIN defect_types_new d ON d.id = i.defect_1_id
     WHERE e.factory_id = :factoryId
       AND e.fy = :fy
       AND e.quarter = :quarter
     GROUP BY e.model_name

     UNION ALL

     SELECT e.model_name AS modelName, COUNT(d.id) AS count
     FROM hfpi_events e
     JOIN hfpi_items i ON i.event_id = e.id
     JOIN defect_types_new d ON d.id = i.defect_2_id
     WHERE e.factory_id = :factoryId
       AND e.fy = :fy
       AND e.quarter = :quarter
     GROUP BY e.model_name

     UNION ALL

     SELECT e.model_name AS modelName, COUNT(d.id) AS count
     FROM hfpi_events e
     JOIN hfpi_items i ON i.event_id = e.id
     JOIN defect_types_new d ON d.id = i.defect_3_id
     WHERE e.factory_id = :factoryId
       AND e.fy = :fy
       AND e.quarter = :quarter
     GROUP BY e.model_name
 """, nativeQuery = true)
 List<Object[]> countDefectsByModelRaw(
         @Param("factoryId") Long factoryId,
         @Param("fy") String fy,
         @Param("quarter") String quarter
 );



}
