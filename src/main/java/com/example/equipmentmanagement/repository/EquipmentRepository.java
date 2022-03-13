package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {
    Optional<Equipment> findByQrcode(String qrcode);
    Page<Equipment> getByNameContains(String name, Pageable pageable);
    Page<Equipment> findAllByNameContainsOrQrcodeContains(String name, String qrcode, Pageable pageable);
    Page<Equipment> getByQrcodeContainsAndMaintenanceIsNull(String qrcode, Pageable pageable);
    Page<Equipment> getByQrcodeContains(String qrcode, Pageable pageable);
    Set<Equipment> findAllByQrcodeIn(Collection<String> qrcode);
}
