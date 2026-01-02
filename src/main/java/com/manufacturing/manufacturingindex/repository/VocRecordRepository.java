package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.VocRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocRecordRepository extends JpaRepository<VocRecord, Long> {

    List<VocRecord> findByFactoryOrderByCreatedAtDesc(Factory factory);

    boolean existsByFactoryAndFyAndQuarterAndMonthRef(
            Factory factory,
            String fy,
            String quarter,
            String monthRef
    );
}
