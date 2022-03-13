package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface MaintenanceRepository extends JpaRepository<Maintenance, String> {
    Page<Maintenance> findAllByDateMaintenanceGreaterThanEqualAndDateMaintenanceLessThanEqual(Date dateMaintenance, Date dateMaintenance2, Pageable pageable);
    Page<Maintenance> getByUser(User user, Pageable pageable);
    List<Maintenance> findAllByDateMaintenanceGreaterThanEqual(Date dateMaintenance);
}
