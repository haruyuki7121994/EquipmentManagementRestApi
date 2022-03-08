package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class EquipmentService implements EquipmentImpl{
    @Autowired
    EquipmentRepository repository;

    @Override
    public String generateQrcode(String prefix) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return "SCALE" + (prefix == null ? "" : prefix) + "-" + RandomStringUtils.randomAlphabetic(4) + "-" + timestamp.getTime();
    }

    @Override
    public Equipment handleSave(EquipmentRequest equipmentRequest, Category category) {
        Equipment equipment = new Equipment();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String name = equipmentRequest.getName();
            String id = timestamp.getTime() + "-em" + RandomStringUtils.randomAlphabetic(4);
            String qrcode = equipmentRequest.getQrcode();

            equipment.setId(id);
            equipment.setQrcode(qrcode);
            equipment.setCreatedAt(new Timestamp(System.currentTimeMillis() + 1));
            equipment.setName(name);
            equipment.setStatus(equipmentRequest.getStatus());
            equipment.setHeight(equipmentRequest.getHeight());
            equipment.setWidth(equipmentRequest.getWidth());
            equipment.setRange(equipmentRequest.getRange());
            equipment.setResolution(equipmentRequest.getResolution());
            equipment.setWeight(equipmentRequest.getWeight());
            equipment.setCategory(category);
            equipment.setDescription(equipmentRequest.getDescription());
            equipment.setLocation(equipmentRequest.getLocation());
            repository.save(equipment);

            return equipment;
        } catch (Exception e) {
            return null;
        }
    }
}
