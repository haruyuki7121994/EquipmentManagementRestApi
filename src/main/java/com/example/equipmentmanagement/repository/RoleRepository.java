package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.ERole;
import com.example.equipmentmanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
