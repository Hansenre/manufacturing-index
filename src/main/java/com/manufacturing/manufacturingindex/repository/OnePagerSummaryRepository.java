package com.manufacturing.manufacturingindex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manufacturing.manufacturingindex.model.OnePagerSummary;

public interface OnePagerSummaryRepository
        extends JpaRepository<OnePagerSummary, Long> {

    // =========================
    // ONE PAGER – EXECUTIVE SUMMARY
    // =========================
    @Query("""
        SELECT s
        FROM OnePagerSummary s
        WHERE s.factory.id = :factoryId
          AND s.fy = :fy
          AND s.quarter = :quarter
    """)
    OnePagerSummary findByFactoryIdAndFyAndQuarter(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    // =========================
    // LEGACY (opcional – manter se usado em outro lugar)
    // =========================
    @Query("""
        SELECT s
        FROM OnePagerSummary s
        WHERE s.factory.id = :factoryId
        ORDER BY s.id DESC
    """)
    OnePagerSummary findLatestByFactoryId(
            @Param("factoryId") Long factoryId
    );
}
