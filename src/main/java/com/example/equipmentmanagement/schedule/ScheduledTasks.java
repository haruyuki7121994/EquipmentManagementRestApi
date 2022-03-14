package com.example.equipmentmanagement.schedule;

import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import com.example.equipmentmanagement.entity.Maintenance;
import com.example.equipmentmanagement.entity.Notification;
import com.example.equipmentmanagement.repository.BulkEquipmentRepository;
import com.example.equipmentmanagement.repository.MaintenanceRepository;
import com.example.equipmentmanagement.repository.NotificationRepository;
import com.example.equipmentmanagement.service.BulkEquipmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    BulkEquipmentRepository bulkEquipmentRepository;
    @Autowired
    BulkEquipmentImpl bulkService;
    @Autowired
    MaintenanceRepository maintenanceRepository;
    @Autowired
    NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 60 * 1000)
    public void executeBulkEquipment() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        BulkEquipmentLog bulkEquipmentlog = bulkEquipmentRepository.findByStatus(0);
        if (bulkEquipmentlog != null) {
            log.info("Processing bulkEquipmentlog id: {}", bulkEquipmentlog.getId());
            if (bulkService.executeLog(bulkEquipmentlog)) {
                log.info("Completed bulkEquipmentlog id: {}", bulkEquipmentlog.getId());
            }
        }

    }

    @Scheduled(fixedRate = 86400 * 1000)
    public void executeMaintenance() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<Maintenance> maintenances = maintenanceRepository.findAllByDateMaintenance(new java.sql.Date(System.currentTimeMillis()));
        for (Maintenance maintenance :
                maintenances) {
            if (maintenance.getStatus() == 0 || maintenance.getStatus() == 2) continue;

            Notification notification = new Notification();
            notification.setRead(false);
            notification.setMaintenance(maintenance);
            notification.setDescription("Today is maintenance day! Please check the maintenance schedule!");
            notification.setTitle("Auto notification");
            notification.setCreatedAt(timestamp);
            notification.setId(timestamp.getTime() + "-notification");
            notificationRepository.save(notification);

            if (!maintenance.getRepeatable()) {
                maintenance.setStatus(2);
            } else {
                maintenance.setLastdateMaintenance(maintenance.getDateMaintenance());
                LocalDate date = maintenance.getDateMaintenance().toLocalDate();
                switch (maintenance.getRepeatedType()) {
                    case 0:
                        date = date.plusDays(7);
                        break;
                    case 1:
                        date = date.plusMonths(1);
                        break;
                    case 2:
                        date = date.plusMonths(3);
                        break;
                    case 3:
                        date = date.plusYears(1);
                        break;
                    default: return;
                }
                maintenance.setDateMaintenance(java.sql.Date.valueOf(date));
            }
            maintenanceRepository.save(maintenance);
        }

    }
}
