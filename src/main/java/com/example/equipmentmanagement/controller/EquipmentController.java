package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.repository.CategoryRepository;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.github.slugify.Slugify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody EquipmentRequest equipmentRequest) {
        Category category;
        Optional<Category> categoryOpt = categoryRepository.findById(equipmentRequest.getCategory_id());
        if (categoryOpt.isPresent()) {
           category = categoryOpt.get();
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Category not found!",
                            null
                    )
            );
        }

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String name = equipmentRequest.getName();
            Slugify slg = new Slugify();
            String slug = slg.slugify(name);
            String id = "equipment-" + timestamp.getTime();
            String qrcode = id + "-" + slug;

            Equipment equipment = new Equipment();
            equipment.setId(id);
            equipment.setQrcode(qrcode);
            equipment.setCreatedAt(new java.sql.Date(timestamp.getTime()));
            equipment.setName(name);
            equipment.setHeight(equipmentRequest.getHeight());
            equipment.setWidth(equipmentRequest.getWidth());
            equipment.setRange(equipmentRequest.getRange());
            equipment.setResolution(equipmentRequest.getResolution());
            equipment.setWeight(equipmentRequest.getWeight());
            equipmentRepository.save(equipment);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Create successful!",
                            equipment
                    )
            );
        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    new MessageResponse(
//                            HttpStatus.BAD_REQUEST.value(),
//                            "Category not found!",
//                            e.getMessage()
//                    )
//            );
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/qrcode/{qrcode}")
    public ResponseEntity<?> findByQrCode(@PathVariable("qrcode") String qrcode) {
        System.out.println("Qrcode:" + qrcode);
        Optional<Equipment> equipmentOptional = equipmentRepository.findByQrcode(qrcode);
        if (equipmentOptional.isPresent()) {
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Find successful!",
                            equipmentOptional.get()
                    )
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
