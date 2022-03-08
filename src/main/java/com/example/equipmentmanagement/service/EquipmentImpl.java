package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;

public interface EquipmentImpl {
    String generateQrcode(String prefix);
    Equipment handleSave(EquipmentRequest equipmentRequest, Category category);
}
