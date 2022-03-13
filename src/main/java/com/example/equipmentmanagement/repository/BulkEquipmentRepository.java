package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface BulkEquipmentRepository extends JpaRepository<BulkEquipmentLog, Integer> {
    BulkEquipmentLog findByStatus(Integer status);
    Page<BulkEquipmentLog> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(Timestamp createdAt, Timestamp createdAt2, Pageable pageable);
}
