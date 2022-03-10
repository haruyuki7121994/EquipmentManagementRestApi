package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, String> {
    Page<Maintenance> getByUser(User user, Pageable pageable);
}
