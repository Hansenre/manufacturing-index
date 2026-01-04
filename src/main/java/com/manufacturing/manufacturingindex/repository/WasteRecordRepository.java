package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.WasteRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WasteRecordRepository extends JpaRepository<WasteRecord, Long> {

    @Query("""
        SELECT w.month, w.wasteGrPairs
        FROM WasteRecord w
        WHERE w.factory.id = :factoryId
          AND w.fy = :fy
          AND w.quarter = :quarter
        ORDER BY w.createdAt DESC
    """)
    List<Object[]> findLast6WasteByFyAndQuarter(@Param("factoryId") Long factoryId,
                                               @Param("fy") String fy,
                                               @Param("quarter") String quarter,
                                               Pageable pageable);

    @Query("""
        SELECT w.month, w.wasteGrPairs
        FROM WasteRecord w
        WHERE w.factory.id = :factoryId
          AND w.fy = :fy
        ORDER BY w.createdAt DESC
    """)
    List<Object[]> findLast6WasteByFy(@Param("factoryId") Long factoryId,
                                     @Param("fy") String fy,
                                     Pageable pageable);

    // ✅ aqui está o ponto: é Month (campo do entity), não MonthRef
    Optional<WasteRecord> findTopByFactoryIdAndFyAndQuarterAndMonthOrderByCreatedAtDesc(
            Long factoryId, String fy, String quarter, String month
    );
}
