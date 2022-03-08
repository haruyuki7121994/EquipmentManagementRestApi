package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.Image;
import com.example.equipmentmanagement.repository.ImageRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class ImageService implements ImageImpl{
    @Autowired
    ImageRepository repository;

    @Override
    public Image handleSave(Equipment equipment, String path) {
        try {
            Image img = new Image();
            img.setId("img" + RandomStringUtils.randomAlphabetic(3) + "-" + new Timestamp(System.currentTimeMillis()).getTime());
            img.setName("img-" + equipment.getId());
            img.setPath(path);
            img.setEquipment(equipment);
            repository.save(img);
            return img;
        } catch (Exception e) {
            return null;
        }
    }
}
