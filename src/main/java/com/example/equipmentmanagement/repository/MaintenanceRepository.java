package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, String> {
}
