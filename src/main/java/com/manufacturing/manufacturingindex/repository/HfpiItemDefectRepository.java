package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.HfpiItemDefect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HfpiItemDefectRepository extends JpaRepository<HfpiItemDefect, Long> {

    @Query("""
        SELECT d.defectType.name, d.severity, COUNT(d)
        FROM HfpiItemDefect d
        WHERE d.item.event.factory.id = :factoryId
        GROUP BY d.defectType.name, d.severity
    """)
    List<Object[]> countDefectsBySeverity(@Param("factoryId") Long factoryId);
}
