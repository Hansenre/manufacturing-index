package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.DefectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefectTypeRepository extends JpaRepository<DefectType, Long> {

	Optional<DefectType> findByCode(String code);
	
    Optional<DefectType> findByName(String name);

}
