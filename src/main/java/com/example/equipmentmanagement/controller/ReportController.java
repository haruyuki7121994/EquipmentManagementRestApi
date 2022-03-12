package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.repository.CategoryRepository;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.example.equipmentmanagement.repository.MaintenanceRepository;
import com.example.equipmentmanagement.repository.UserRepository;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired
    ResponseImpl responseService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    MaintenanceRepository maintenanceRepository;

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        int totalMaintainers = userRepository.countAllByRole("ROLE_MAINTAINER");
        int totalCategories = categoryRepository.findAll().size();
        int totalEquipments =  equipmentRepository.findAll().size();
        int totalMaintenances = maintenanceRepository.findAll().size();
        Map<String, Integer> data = new HashMap<>();
        data.put("maintainers", totalMaintainers);
        data.put("categories", totalCategories);
        data.put("equipments", totalEquipments);
        data.put("maintenances", totalMaintenances);
        return responseService.success("Report successful!", data);
    }
}
