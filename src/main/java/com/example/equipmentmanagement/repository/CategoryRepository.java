package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByName(String name);
    Boolean existsByName(String name);
    Boolean existsByNameAndIdIsNotIn(String name, Collection<String> id);
    Page<Category> getByNameContains(String name, Pageable pageable);
    Page<Category> getByIsActive(Boolean isActive, Pageable pageable);
}
