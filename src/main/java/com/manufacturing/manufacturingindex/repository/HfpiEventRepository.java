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
}
