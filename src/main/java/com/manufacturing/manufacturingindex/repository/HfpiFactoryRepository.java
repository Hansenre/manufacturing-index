package com.manufacturing.manufacturingindex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manufacturing.manufacturingindex.model.HfpiFactory;

public interface HfpiFactoryRepository extends JpaRepository<HfpiFactory, Long> {

    @Query("""
        SELECT h
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
        ORDER BY h.fy DESC, h.quarter DESC, h.month ASC, h.id DESC
    """)
    List<HfpiFactory> findAllByFactory(@Param("factoryId") Long factoryId);

    @Query("""
        SELECT h
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
          AND (:fy IS NULL OR h.fy = :fy)
          AND (:quarter IS NULL OR h.quarter = :quarter)
        ORDER BY h.month ASC, h.id DESC
    """)
    List<HfpiFactory> findByFactoryAndFyQuarter(
            @Param("factoryId") Long factoryId,
            @Param("fy") String fy,
            @Param("quarter") String quarter
    );

    @Query("""
        SELECT DISTINCT h.fy
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
        ORDER BY h.fy DESC
    """)
    List<String> listFy(@Param("factoryId") Long factoryId);

    @Query("""
        SELECT DISTINCT h.quarter
        FROM HfpiFactory h
        WHERE h.factory.id = :factoryId
          AND (:fy IS NULL OR h.fy = :fy)
        ORDER BY h.quarter ASC
    """)
    List<String> listQuarter(@Param("factoryId") Long factoryId, @Param("fy") String fy);
    
    Optional<HfpiFactory> findTopByFactoryIdOrderByFyDescQuarterDescIdDesc(Long factoryId);
    
    @Query(value = """
    	    SELECT
    	        COALESCE(SUM(hfpi_aprovados), 0) AS aprovados,
    	        COALESCE(SUM(hfpi_realizado), 0) AS realizados,
    	        ROUND(
    	            COALESCE(SUM(hfpi_aprovados), 0) * 100.0 /
    	            NULLIF(COALESCE(SUM(hfpi_realizado), 0), 0),
    	            2
    	        ) AS hfpi_factory_percent
    	    FROM hfpi_factory
    	    WHERE factory_id = :factoryId
    	      AND fy = :fy
    	      AND quarter = :quarter
    	""", nativeQuery = true)
    	Object[] getHfpiFactoryPercent(
    	        @Param("factoryId") Long factoryId,
    	        @Param("fy") String fy,
    	        @Param("quarter") String quarter
    	);




}
