package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;

public interface KpiRecordRepository extends JpaRepository<KpiRecord, Long> {

    List<KpiRecord> findByFactory(Factory factory);
    long countByFactoryId(Long factoryId);
    List<KpiRecord> findByFactoryAndFyAndQuarter(
            Factory factory,
            String fy,
            String quarter
    );
}
