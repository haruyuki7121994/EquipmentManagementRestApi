package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.aws.AmazonClient;
import com.example.equipmentmanagement.dto.BulkEquipmentsRequest;
import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.*;
import com.example.equipmentmanagement.service.EquipmentImpl;
import com.example.equipmentmanagement.service.ImageImpl;
import com.example.equipmentmanagement.service.PagingImpl;
import com.example.equipmentmanagement.service.ResponseImpl;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
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
    CommentRepository commentRepository;
    @Autowired
    AmazonClient amazonClient;
    @Autowired
    EquipmentImpl service;
    @Autowired
    BulkEquipmentRepository logRepository;
    @Autowired
    ImageImpl imageService;
    @Autowired
    PagingImpl pagingService;
    @Autowired
    ResponseImpl responseService;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String keyword
    ) {
        try {
            List<Equipment> equipments;
            String[] parts = orderBy.split("-");

            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );

            Page<Equipment> pageEquips;
            pageEquips = equipmentRepository.findAllByNameContainsOrQrcodeContains(keyword, keyword, paging);

            equipments = pageEquips.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("equipments", equipments);

            Map<String, Object> metadata = pagingService.getMetadata(pageEquips);

            return responseService.successWithPaging(metadata, response);
        } catch (Exception e) {
            return responseService.serverError();
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
            return responseService.badRequest("Category not found!");
        }

        if (equipmentRequest.getQrcode() == null || equipmentRequest.getQrcode().isEmpty()) {
            equipmentRequest.setQrcode(service.generateQrcode(null));
        } else {
            equipmentRequest.setQrcode(equipmentRequest.getQrcode());
        }

        Equipment result = service.handleSave(equipmentRequest, category);

        if (result != null) {
            uploadImage(equipmentRequest.getImages(), result);
        }

        return result == null ?
                responseService.serverError() :
                responseService.success("Create equipment successful!", result);
    }

    @GetMapping("/qrcode/{qrcode}")
    public ResponseEntity<?> findByQrCode(@PathVariable("qrcode") String qrcode) {
        Optional<Equipment> equipmentOptional = equipmentRepository.findByQrcode(qrcode);

        return equipmentOptional.isPresent() ?
                responseService.success("Find successful!", equipmentOptional.get()) :
                responseService.notFound();
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
            return responseService.badRequest("Category not found!");
        }

        Optional<Equipment> equipmentOptional = equipmentRepository.findById(id);
        if (equipmentOptional.isEmpty()) {
            return responseService.serverError();
        }

        Equipment equipment = equipmentOptional.get();
        try {
            String name = equipmentRequest.getName();
            equipment.setName(name);
            equipment.setStatus(equipmentRequest.getStatus());
            equipment.setHeight(equipmentRequest.getHeight());
            equipment.setWidth(equipmentRequest.getWidth());
            equipment.setRange(equipmentRequest.getRange());
            equipment.setResolution(equipmentRequest.getResolution());
            equipment.setWeight(equipmentRequest.getWeight());
            equipment.setCategory(category);
            equipment.setLocation(equipmentRequest.getLocation());
            equipment.setDescription(equipmentRequest.getDescription());
            equipmentRepository.save(equipment);
        } catch (Exception e) {
            return responseService.serverError();
        }

        uploadImage(equipmentRequest.getImages(), equipment);

        return responseService.success("Update equipment successful!", equipment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        Optional<Equipment> equipmentOptional = equipmentRepository.findById(id);
        if (equipmentOptional.isEmpty()) {
            return responseService.notFound();
        }

        try {
            Equipment equipment = equipmentOptional.get();

            Set<Comment> comments = equipment.getComments();
            commentRepository.deleteAll(comments);

            equipmentRepository.delete(equipment);
            return responseService.success("Delete successful!", null);
        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
            return responseService.serverError();
        }
    }

    @PostMapping(value = "/bulk-create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> bulkCreate(
            @ModelAttribute BulkEquipmentsRequest bulkEquipmentsRequest
    ) {
        Optional<Category> categoryOpt = categoryRepository.findById(bulkEquipmentsRequest.getCategory_id());
        if (categoryOpt.isEmpty()) {
            return responseService.badRequest("Category not found!");
        }

        String strQrList = "";
        Integer qty = bulkEquipmentsRequest.getQuantity();
        String type = bulkEquipmentsRequest.getType_qrcode();
        Set<String> qrcodeList = new LinkedHashSet<>();
        if (type.equals("auto")) {
            while (qrcodeList.size() < qty) {
                String qrcode = service.generateQrcode(bulkEquipmentsRequest.getPrefix());
                qrcodeList.add(qrcode);
            }
        } else {
            qrcodeList = bulkEquipmentsRequest.getManual_qrcode();
        }
        if (!qrcodeList.isEmpty()) strQrList = new Gson().toJson(qrcodeList);

        String strPaths = "";
        Set<String> images = new HashSet<>();
        List.of(bulkEquipmentsRequest.getImages()).forEach(file -> {
            if (!file.isEmpty()) {
                String path = amazonClient.uploadFile(file);
                images.add(path);
            }
        });
        if (!images.isEmpty()) strPaths = new Gson().toJson(images);

        String strData = "";
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", bulkEquipmentsRequest.getName());
        data.put("status", bulkEquipmentsRequest.getStatus());
        data.put("width", bulkEquipmentsRequest.getWidth());
        data.put("height", bulkEquipmentsRequest.getHeight());
        data.put("range", bulkEquipmentsRequest.getRange());
        data.put("resolution", bulkEquipmentsRequest.getResolution());
        data.put("description", bulkEquipmentsRequest.getDescription());
        data.put("location", bulkEquipmentsRequest.getLocation());
        data.put("weight", bulkEquipmentsRequest.getWeight());
        data.put("category_id", bulkEquipmentsRequest.getCategory_id());
        strData = new Gson().toJson(data);

        BulkEquipmentLog log = new BulkEquipmentLog();
        log.setQuantity(bulkEquipmentsRequest.getQuantity());
        log.setImagePaths(strPaths);
        log.setData(strData);
        log.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        log.setQrcodeList(strQrList);
        log.setStatus(0);
        logRepository.save(log);

        return responseService.success("Log Created Successful!", null);
    }

    private void uploadImage(MultipartFile[] files, Equipment equipment) {
        Set<Image> images = new HashSet<>();
        List.of(files).forEach(file -> {
            if (!file.isEmpty()) {
                String path = amazonClient.uploadFile(file);
                Image img = imageService.handleSave(equipment, path);
                images.add(img);
            }
        });
        if (!images.isEmpty()) equipment.setImages(images);
    }
}
