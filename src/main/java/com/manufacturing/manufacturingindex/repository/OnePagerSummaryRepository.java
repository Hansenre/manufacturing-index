package com.manufacturing.manufacturingindex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manufacturing.manufacturingindex.model.OnePagerSummary;

public interface OnePagerSummaryRepository
        extends JpaRepository<OnePagerSummary, Long> {

    @Query("""
        SELECT s
        FROM OnePagerSummary s
        WHERE s.factory.id = :factoryId
        """)
    OnePagerSummary findByFactoryId(@Param("factoryId") Long factoryId);
}
