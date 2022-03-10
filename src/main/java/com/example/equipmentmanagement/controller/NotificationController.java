package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.NotificationRequest;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.Notification;
import com.example.equipmentmanagement.repository.MaintenanceRepository;
import com.example.equipmentmanagement.repository.NotificationRepository;
import com.example.equipmentmanagement.service.PagingImpl;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    PagingImpl pagingService;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String username
    ) {
        try {
            List<Notification> notifications;
            String[] parts = orderBy.split("-");

            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );

            Page<Notification> pageNotification;
            if (username.isEmpty()) {
                pageNotification = repository.findAll(paging);
            } else {
                pageNotification = repository.getByUsername(username, paging);
            }


            notifications = pageNotification.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);

            Map<String, Object> metadata = pagingService.getMetadata(pageNotification);

            return responseService.successWithPaging(metadata, response);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody NotificationRequest notificationRequest) {
        Optional<Maintenance> maintenanceOptional = maintenanceRepository.findById(notificationRequest.getMaintenance_id());
        if (maintenanceOptional.isEmpty()) {
            return responseService.badRequest("Maintenance not found");
        }
        Maintenance maintenance = maintenanceOptional.get();
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Notification notification = new Notification();
            notification.setId(timestamp.getTime() + "-notification");
            notification.setRead(false);
            notification.setTitle(notificationRequest.getTitle());
            notification.setDescription(notificationRequest.getDescription());
            notification.setCreatedAt(new Date(timestamp.getTime()));
            notification.setMaintenance(maintenance);
            repository.save(notification);
            return responseService.success("Create successful!", notification);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
//            return responseService.serverError();
        }
    }
}
