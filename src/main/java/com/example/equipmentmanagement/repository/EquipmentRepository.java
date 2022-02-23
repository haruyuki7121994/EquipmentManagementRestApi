package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {
    Optional<Equipment> findByQrcode(String qrcode);
}
