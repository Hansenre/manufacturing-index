package com.manufacturing.manufacturingindex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manufacturing.manufacturingindex.model.VocRecord;

public interface VocRecordRepository extends JpaRepository<VocRecord, Long> {

    List<VocRecord> findByFactoryIdOrderByCreatedAtDesc(Long factoryId);

    Optional<VocRecord> findTopByFactoryIdOrderByCreatedAtDesc(Long factoryId);

    Optional<VocRecord> findByFactoryIdAndFyAndQuarterAndMonthRef(
            Long factoryId, String fy, String quarter, String monthRef
    );
}
