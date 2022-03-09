package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.NotificationRequest;
import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.Notification;
import com.example.equipmentmanagement.repository.MaintenanceRepository;
import com.example.equipmentmanagement.repository.NotificationRepository;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    ResponseImpl responseService;
    @Autowired
    NotificationRepository repository;
    @Autowired
    MaintenanceRepository maintenanceRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody NotificationRequest notificationRequest) {
        Optional<Maintenance> maintenanceOptional = maintenanceRepository.findById(notificationRequest.getId());
        if (maintenanceOptional.isEmpty()) {
            return responseService.badRequest("Maintenance not found");
        }
        Maintenance maintenance = maintenanceOptional.get();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Notification notification = new Notification();
            notification.setId(timestamp.getTime() + "-notification");
            notification.setTitle(notification.getTitle());
            notification.setDescription(notification.getDescription());
            notification.setCreatedAt(new Date(timestamp.getTime()));
            notification.setMaintenance(maintenance);
            repository.save(notification);
            return responseService.success("Create successful!", notification);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }
}
