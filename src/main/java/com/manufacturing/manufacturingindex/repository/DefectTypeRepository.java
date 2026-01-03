package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.DefectType;
import org.springframework.data.domain.Pageable;
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
    // ONE PAGER – FY + QUARTER (mantido)
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

    // ==========================================================
    // ✅ NOVO: DR – TOP TYPES (One Pager) -> [name, total, rate]
    // ==========================================================
    @Query("""
        SELECT x.name,
               x.total,
               (x.total * 100.0) / NULLIF(t.grandTotal, 0)
        FROM (
            SELECT d.name AS name, COUNT(d) AS total
            FROM HfpiItem i
            JOIN i.defect1 d
            WHERE i.event.factory.id = :factoryId
              AND i.event.fy = :fy
              AND i.event.quarter = :quarter
            GROUP BY d.name

            UNION ALL

            SELECT d.name AS name, COUNT(d) AS total
            FROM HfpiItem i
            JOIN i.defect2 d
            WHERE i.event.factory.id = :factoryId
              AND i.event.fy = :fy
              AND i.event.quarter = :quarter
            GROUP BY d.name

            UNION ALL

            SELECT d.name AS name, COUNT(d) AS total
            FROM HfpiItem i
            JOIN i.defect3 d
            WHERE i.event.factory.id = :factoryId
              AND i.event.fy = :fy
              AND i.event.quarter = :quarter
            GROUP BY d.name
        ) u
        JOIN (
            SELECT SUM(z.total) AS grandTotal
            FROM (
                SELECT COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect1 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter

                UNION ALL

                SELECT COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect2 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter

                UNION ALL

                SELECT COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect3 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter
            ) z
        ) t ON 1=1
        JOIN (
            SELECT u2.name AS name, SUM(u2.total) AS total
            FROM (
                SELECT d.name AS name, COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect1 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter
                GROUP BY d.name

                UNION ALL

                SELECT d.name AS name, COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect2 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter
                GROUP BY d.name

                UNION ALL

                SELECT d.name AS name, COUNT(d) AS total
                FROM HfpiItem i JOIN i.defect3 d
                WHERE i.event.factory.id = :factoryId
                  AND i.event.fy = :fy
                  AND i.event.quarter = :quarter
                GROUP BY d.name
            ) u2
            GROUP BY u2.name
        ) x ON x.name = u.name
        GROUP BY x.name, x.total, t.grandTotal
        ORDER BY x.total DESC
    """)
    List<Object[]> getDrTopTypes(@Param("factoryId") Long factoryId,
                                @Param("fy") String fy,
                                @Param("quarter") String quarter,
                                Pageable pageable);

    // ✅ Overload simples (o OnePagerService chama esse)
    default List<Object[]> getDrTopTypes(Long factoryId, String fy, String quarter) {
        return getDrTopTypes(factoryId, fy, quarter, org.springframework.data.domain.PageRequest.of(0, 5));
    }
}
