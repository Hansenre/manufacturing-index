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
       🍕 Defeitos por descrição (MANTIDO – SEM FY/QUARTER)
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
       📊 Defeitos por severidade (MANTIDO)
       ===================================================== */
    @Query("""
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect1 d
        WHERE e.factory.id = :factoryId
          AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating

        UNION ALL
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect2 d
        WHERE e.factory.id = :factoryId
          AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating

        UNION ALL
        SELECT d.name, i.rating, COUNT(d)
        FROM HfpiEvent e
        JOIN e.items i
        JOIN i.defect3 d
        WHERE e.factory.id = :factoryId
          AND i.rating <> 'BOM'
        GROUP BY d.name, i.rating
    """)
    List<Object[]> countDefectsBySeverity(@Param("factoryId") Long factoryId);

    /* =====================================================
    📊 PARETO 80/20 – Defeitos por TIPO (FY + QUARTER)
    🔥 VALIDADO NO BANCO
    ===================================================== */
 @Query(value = """
     SELECT defect_name, SUM(cnt) AS total
     FROM (
         SELECT d.name AS defect_name, COUNT(*) AS cnt
         FROM hfpi_items i
         JOIN hfpi_events e ON e.id = i.event_id
         JOIN defect_types_new d ON d.id = i.defect_1_id
         WHERE e.factory_id = :factoryId
           AND e.fy = :fy
           AND e.quarter = :quarter
         GROUP BY d.name

         UNION ALL

         SELECT d.name, COUNT(*)
         FROM hfpi_items i
         JOIN hfpi_events e ON e.id = i.event_id
         JOIN defect_types_new d ON d.id = i.defect_2_id
         WHERE e.factory_id = :factoryId
           AND e.fy = :fy
           AND e.quarter = :quarter
         GROUP BY d.name

         UNION ALL

         SELECT d.name, COUNT(*)
         FROM hfpi_items i
         JOIN hfpi_events e ON e.id = i.event_id
         JOIN defect_types_new d ON d.id = i.defect_3_id
         WHERE e.factory_id = :factoryId
           AND e.fy = :fy
           AND e.quarter = :quarter
         GROUP BY d.name
     ) t
     GROUP BY defect_name
     ORDER BY total DESC
 """, nativeQuery = true)
 List<Object[]> countDefectsParetoByType(
         @Param("factoryId") Long factoryId,
         @Param("fy") String fy,
         @Param("quarter") String quarter
 );


    /* =====================================================
       🔽 FY / Quarter (MANTIDO)
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
       👟 MODELOS DISPONÍVEIS (MANTIDO)
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
       📊 PARETO 80/20 – Defeitos por MODELO (MANTIDO)
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

    /* =====================================================
    HFPI ONLINE – ITENS (MODERADO + SEVERO)
    FY + QUARTER ISOLADO
    ===================================================== */
    @Query(value = """
    	    SELECT
    	        COUNT(i.id) AS total_itens,
    	        SUM(CASE WHEN i.rating IN ('MODERADO','SEVERO') THEN 1 ELSE 0 END) AS itens_ruins,
    	        ROUND(
    	            (COUNT(i.id) - SUM(CASE WHEN i.rating IN ('MODERADO','SEVERO') THEN 1 ELSE 0 END))
    	            * 100.0 / NULLIF(COUNT(i.id), 0),
    	            2
    	        ) AS hfpi_online_percent
    	    FROM hfpi_events e
    	    JOIN hfpi_items i ON i.event_id = e.id
    	    WHERE e.factory_id = :factoryId
    	      AND e.fy = :fy
    	      AND e.quarter = :quarter
    	""", nativeQuery = true)
    	Object[] getHfpiOnlinePercent(
    	        @Param("factoryId") Long factoryId,
    	        @Param("fy") String fy,
    	        @Param("quarter") String quarter
    	);

    @Query(value = """
    	    SELECT
    	        COUNT(i.id) AS total_itens,
    	        SUM(CASE WHEN i.rating <> 'BOM' THEN 1 ELSE 0 END) AS itens_ruins
    	    FROM hfpi_events e
    	    JOIN hfpi_items i ON i.event_id = e.id
    	    WHERE e.factory_id = :factoryId
    	      AND e.fy = :fy
    	      AND e.quarter = :quarter
    	""", nativeQuery = true)
    	Object[] getHfpiOnlineByItems(
    	        @Param("factoryId") Long factoryId,
    	        @Param("fy") String fy,
    	        @Param("quarter") String quarter
    	);
    

     
    
    
}
