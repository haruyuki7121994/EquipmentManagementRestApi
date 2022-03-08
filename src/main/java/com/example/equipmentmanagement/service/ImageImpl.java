package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.Image;

public interface ImageImpl {
    Image handleSave(Equipment equipment, String path);
}
