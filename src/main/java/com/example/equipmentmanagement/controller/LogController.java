package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import com.example.equipmentmanagement.entity.Comment;
import com.example.equipmentmanagement.repository.BulkEquipmentRepository;
import com.example.equipmentmanagement.service.PagingImpl;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/logs")
public class LogController {
    @Autowired
    ResponseImpl responseService;
    @Autowired
    PagingImpl pagingService;
    @Autowired
    BulkEquipmentRepository repository;
    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate
    ) {
        try {
            String[] parts = orderBy.split("-");
            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );
            Page<BulkEquipmentLog> paginator;

            if (startDate.isEmpty() || endDate.isEmpty()) {
                paginator = repository.findAll(paging);
            } else {
                paginator = repository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanEqual(
                        new java.sql.Timestamp(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm").parse(startDate).getTime()),
                        new java.sql.Timestamp(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm").parse(endDate).getTime()),
                        paging
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("logs", paginator.getContent());

            Map<String, Object> metadata = pagingService.getMetadata(paginator);

            return responseService.successWithPaging(metadata, response);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
        }
    }
}
