package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkEquipmentRepository extends JpaRepository<BulkEquipmentLog, Integer> {
    BulkEquipmentLog findByStatus(Integer status);
}
