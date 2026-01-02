package com.manufacturing.manufacturingindex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.User;

public interface FactoryRepository extends JpaRepository<Factory, Long> {

    List<Factory> findByOwner(User owner);

    List<Factory> findByOwnerUsername(String username);
}
