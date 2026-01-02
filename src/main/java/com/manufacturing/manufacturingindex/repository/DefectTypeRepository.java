package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.DefectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DefectTypeRepository extends JpaRepository<DefectType, Long> {

    // =========================
    // BÁSICOS (mantidos)
    // =========================
    Optional<DefectType> findByCode(String code);
    Optional<DefectType> findByName(String name);

    // =========================
    // LEGACY – sem período (mantido)
    // =========================
    @Query("""
        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect1 d
        WHERE i.event.factory.id = :factoryId
        GROUP BY d.name

        UNION ALL

        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect2 d
        WHERE i.event.factory.id = :factoryId
        GROUP BY d.name

        UNION ALL

        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect3 d
        WHERE i.event.factory.id = :factoryId
        GROUP BY d.name
    """)
    List<Object[]> findTopDefects(@Param("factoryId") Long factoryId);

    // =========================
    // ONE PAGER – FY + QUARTER
    // =========================
    @Query("""
        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect1 d
        WHERE i.event.factory.id = :factoryId
          AND i.event.fy = :fy
          AND i.event.quarter = :quarter
        GROUP BY d.name

        UNION ALL

        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect2 d
        WHERE i.event.factory.id = :factoryId
          AND i.event.fy = :fy
          AND i.event.quarter = :quarter
        GROUP BY d.name

        UNION ALL

        SELECT d.name, COUNT(d)
        FROM HfpiItem i
        JOIN i.defect3 d
        WHERE i.event.factory.id = :factoryId
          AND i.event.fy = :fy
          AND i.event.quarter = :quarter
        GROUP BY d.name
    """)
    List<Object[]> findTopDefects(@Param("factoryId") Long factoryId,
                                 @Param("fy") String fy,
                                 @Param("quarter") String quarter);
}
