package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CategoryRequest;
import com.example.equipmentmanagement.dto.MaintenanceRequest;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.User;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.example.equipmentmanagement.repository.MaintenanceRepository;
import com.example.equipmentmanagement.repository.UserRepository;
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
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/maintenances")
public class MaintenanceController {
    @Autowired
    MaintenanceRepository repository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    PagingImpl pagingService;
    @Autowired
    ResponseImpl responseService;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "") String username
    ) {
        try {
            List<Maintenance> maintenances;
            String[] parts = orderBy.split("-");

            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );

            Page<Maintenance> pageMaintenance;
            Optional<User> maintainerOptional = userRepository.findByUsername(username);
            if (maintainerOptional.isPresent()) {
                User user = maintainerOptional.get();
                pageMaintenance = repository.getByUser(user, paging);
            } else {
                if (startDate.isEmpty() || endDate.isEmpty()) {
                    pageMaintenance = repository.findAll(paging);
                } else {
                    pageMaintenance = repository.findAllByDateMaintenanceGreaterThanEqualAndDateMaintenanceLessThanEqual(
                            new Date(new SimpleDateFormat("yyyy-MM-dd").parse(startDate).getTime()),
                            new Date(new SimpleDateFormat("yyyy-MM-dd").parse(endDate).getTime()),
                            paging
                    );
                }

            }

            Map<String, Object> response = new HashMap<>();
            response.put("maintenances", pageMaintenance.getContent());

            Map<String, Object> metadata = pagingService.getMetadata(pageMaintenance);

            return responseService.successWithPaging(metadata, response);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable("id") String id) {
        Optional<Maintenance> maintenanceOptional = repository.findById(id);
        if (maintenanceOptional.isPresent()) {
            return responseService.success("Find successful!", maintenanceOptional.get());
        }
        return responseService.notFound();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody MaintenanceRequest maintenanceRequest) {
        Optional<User> userOptional = userRepository.findById(maintenanceRequest.getMaintainer());
        if (userOptional.isEmpty()) {
            return responseService.badRequest("Maintainer not found!");
        }
        User user = userOptional.get();
        try {
            Maintenance maintenance = new Maintenance();
            maintenance.setId(new Timestamp(System.currentTimeMillis()).getTime() + "-maintenance");
            maintenance.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            maintenance.setDateMaintenance(maintenanceRequest.getDateMaintenance());
            maintenance.setRepeatable(maintenanceRequest.getRepeatable());
            maintenance.setRepeatedType(maintenanceRequest.getRepeatedType());
            maintenance.setStatus(maintenanceRequest.getStatus());
            maintenance.setUser(user);
            repository.save(maintenance);
            return responseService.success("Create successful!", maintenance);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody MaintenanceRequest maintenanceRequest) {
        Optional<Maintenance> maintenanceOptional = repository.findById(id);
        if (maintenanceOptional.isPresent()) {
            Maintenance maintenance = maintenanceOptional.get();
            maintenance.setDateMaintenance(maintenanceRequest.getDateMaintenance());
            maintenance.setRepeatable(maintenanceRequest.getRepeatable());
            maintenance.setRepeatedType(maintenanceRequest.getRepeatedType());
            repository.save(maintenance);
            return responseService.success("Update successful!", maintenance);
        } else {
            return responseService.notFound();
        }
    }

    @PostMapping("/{id}/add-equipments")
    public ResponseEntity<?> addEquipments(@PathVariable("id") String id, @Valid @RequestBody MaintenanceRequest maintenanceRequest) {
        Optional<Maintenance> maintenanceOptional = repository.findById(id);
        if (maintenanceOptional.isEmpty()) {
            return responseService.notFound();
        }

        Maintenance maintenance = maintenanceOptional.get();
        try {
            for (String qrcode : maintenanceRequest.getEquipmentListQrcode()) {
                Optional<Equipment> equipmentOptional = equipmentRepository.findByQrcode(qrcode);
                if (equipmentOptional.isEmpty()) continue;
                Equipment equipment = equipmentOptional.get();
                equipment.setMaintenance(maintenance);
                equipmentRepository.save(equipment);
            }
            maintenance.setStatus(1);
            repository.save(maintenance);
            return responseService.success("Add equipments successful!", null);
        } catch (Exception e) {
//            return responseService.badRequest(e.getMessage());
            return responseService.serverError();
        }
    }

    @PostMapping("/{id}/remove-equipments")
    public ResponseEntity<?> removeEquipments(@PathVariable("id") String id, @Valid @RequestBody MaintenanceRequest maintenanceRequest) {
        Optional<Maintenance> maintenanceOptional = repository.findById(id);
        if (maintenanceOptional.isEmpty()) {
            return responseService.notFound();
        }

        Maintenance maintenance = maintenanceOptional.get();
        try {
            int count = 0;
            for (String qrcode : maintenanceRequest.getEquipmentListQrcode()) {
                Optional<Equipment> equipmentOptional = equipmentRepository.findByQrcode(qrcode);
                if (equipmentOptional.isEmpty()) continue;
                Equipment equipment = equipmentOptional.get();
                equipment.setMaintenance(null);
                equipmentRepository.save(equipment);
                count ++;
            }
            if (count == maintenance.getEquipments().size()) {
                maintenance.setStatus(0);
                repository.save(maintenance);
            }
            return responseService.success("Remove equipments successful!", null);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
//            return responseService.serverError();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        try {
            Optional<Maintenance> maintenanceOptional = repository.findById(id);

            if (maintenanceOptional.isEmpty()) {
                return responseService.notFound();
            } else {
                Maintenance maintenance = maintenanceOptional.get();
                Set<Equipment> equipmentSet = maintenance.getEquipments();
                if (equipmentSet.size() > 0) {
                    return responseService.badRequest("Cannot Delete!");
                }

                repository.deleteById(id);

                return responseService.success("Delete successful!", null);
            }
        } catch (Exception e) {
            return responseService.serverError();
        }
    }
}
