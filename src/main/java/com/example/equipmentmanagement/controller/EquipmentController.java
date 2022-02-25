package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.aws.AmazonClient;
import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.dto.PagingMessageResponse;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.Image;
import com.example.equipmentmanagement.repository.CategoryRepository;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.example.equipmentmanagement.repository.ImageRepository;
import com.github.slugify.Slugify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    AmazonClient amazonClient;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {
            List<Equipment> equipments;
            Pageable paging = PageRequest.of(page, size);
            Page<Equipment> pageEquips = equipmentRepository.findAll(paging);
            equipments = pageEquips.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("equipments", equipments);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentPage", pageEquips.getNumber());
            metadata.put("totalItems", pageEquips.getTotalElements());
            metadata.put("totalPages", pageEquips.getTotalPages());

            return ResponseEntity.ok(
                    new PagingMessageResponse(
                            HttpStatus.OK.value(),
                            metadata,
                            response
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> create(
            @ModelAttribute EquipmentRequest equipmentRequest
    ) {
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

        Equipment equipment = new Equipment();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String name = equipmentRequest.getName();
            Slugify slg = new Slugify();
            String slug = slg.slugify(name);
            String id = "equipment-" + timestamp.getTime();
            String qrcode = id + "-" + slug;

            equipment.setId(id);
            equipment.setQrcode(qrcode);
            equipment.setCreatedAt(new java.sql.Date(timestamp.getTime()));
            equipment.setName(name);
            equipment.setHeight(equipmentRequest.getHeight());
            equipment.setWidth(equipmentRequest.getWidth());
            equipment.setRange(equipmentRequest.getRange());
            equipment.setResolution(equipmentRequest.getResolution());
            equipment.setWeight(equipmentRequest.getWeight());
            equipment.setCategory(category);
            equipmentRepository.save(equipment);
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

        Set<Image> images = new HashSet<>();
        List.of(equipmentRequest.getImages()).forEach(file -> {
            Image img = new Image();
            img.setId("image" + new Timestamp(System.currentTimeMillis()));
            img.setName("equipment-image");
            img.setPath(amazonClient.uploadFile(file));
            img.setEquipment(equipment);
            imageRepository.save(img);
            images.add(img);
        });

        equipment.setImages(images);

        return ResponseEntity.ok().body(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        "Create equipment successful!",
                        equipment
                )
        );
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

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") String id,
            @ModelAttribute EquipmentRequest equipmentRequest
    ) {
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

        Optional<Equipment> equipmentOptional = equipmentRepository.findById(id);
        if (equipmentOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Equipment equipment = equipmentOptional.get();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String name = equipmentRequest.getName();
            Slugify slg = new Slugify();
            String slug = slg.slugify(name);
            String qrcode = id + "-" + slug;

            equipment.setId(id);
            equipment.setQrcode(qrcode);
            equipment.setCreatedAt(new java.sql.Date(timestamp.getTime()));
            equipment.setName(name);
            equipment.setHeight(equipmentRequest.getHeight());
            equipment.setWidth(equipmentRequest.getWidth());
            equipment.setRange(equipmentRequest.getRange());
            equipment.setResolution(equipmentRequest.getResolution());
            equipment.setWeight(equipmentRequest.getWeight());
            equipment.setCategory(category);
            equipmentRepository.save(equipment);
            return ResponseEntity.ok().body(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Create equipment successful!",
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        try {
            equipmentRepository.deleteById(id);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Delete successful!",
                            null
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
