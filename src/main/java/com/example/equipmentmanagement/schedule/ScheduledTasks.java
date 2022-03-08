package com.example.equipmentmanagement.schedule;

import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import com.example.equipmentmanagement.repository.BulkEquipmentRepository;
import com.example.equipmentmanagement.service.BulkEquipmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    BulkEquipmentRepository bulkEquipmentRepository;
    @Autowired
    BulkEquipmentImpl bulkService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
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
}
