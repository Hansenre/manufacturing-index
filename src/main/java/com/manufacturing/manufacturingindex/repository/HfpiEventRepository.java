package com.manufacturing.manufacturingindex.repository;

import com.manufacturing.manufacturingindex.model.HfpiEvent;
import com.manufacturing.manufacturingindex.model.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HfpiEventRepository extends JpaRepository<HfpiEvent, Long> {

    List<HfpiEvent> findByFactory(Factory factory);
}
