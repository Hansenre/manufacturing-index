package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    
    @Query("""
    	    SELECT
    	        (SELECT AVG(b.kpiValue)
    	         FROM KpiRecord b
    	         WHERE b.factory.id = :factoryId AND b.type = 'BTP'),

    	        (SELECT AVG(w.points)
    	         FROM KpiRecord w
    	         WHERE w.factory.id = :factoryId),

    	        (SELECT SUM(w2.points)
    	         FROM KpiRecord w2
    	         WHERE w2.factory.id = :factoryId),

    	        (SELECT AVG(m.kpiValue)
    	         FROM KpiRecord m
    	         WHERE m.factory.id = :factoryId AND m.type = 'MQAAS'),

    	        (SELECT AVG(d.kpiValue)
    	         FROM KpiRecord d
    	         WHERE d.factory.id = :factoryId AND d.type = 'DEFECT')
    	""")
    	Object[] calculateOperationKpi(@Param("factoryId") Long factoryId);




}
