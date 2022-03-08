package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.dto.EquipmentRequest;
import com.example.equipmentmanagement.entity.BulkEquipmentLog;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.repository.BulkEquipmentRepository;
import com.example.equipmentmanagement.repository.CategoryRepository;
import com.example.equipmentmanagement.repository.ImageRepository;
import com.example.equipmentmanagement.schedule.ScheduledTasks;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BulkEquipmentService implements BulkEquipmentImpl{
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    EquipmentImpl service;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    BulkEquipmentRepository repository;
    @Autowired
    EquipmentImpl equipmentService;
    @Autowired
    ImageImpl imageService;

    @Override
    public Boolean executeLog(BulkEquipmentLog bulkEquipmentLog) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        AtomicInteger count = new AtomicInteger();

        List<String> images = new Gson().fromJson(bulkEquipmentLog.getImagePaths(), listType);
//        images.forEach(log::info);

        List<String> qrcodeList = new Gson().fromJson(bulkEquipmentLog.getQrcodeList(), listType);
//        qrcodeList.forEach(log::info);

        Map<String, Object> data = new Gson().fromJson(bulkEquipmentLog.getData(), mapType);
//        data.forEach((key, value) -> log.info(key + ":" + value));

        try {
            Optional<Category> categoryOpt = categoryRepository.findById((String) data.get("category_id"));
            if (categoryOpt.isEmpty()) {
                bulkEquipmentLog.setStatus(2);
                repository.save(bulkEquipmentLog);
                return false;
            } else {
                Category category = categoryOpt.get();
                for (String s : qrcodeList) {
                    EquipmentRequest request = new EquipmentRequest();
                    Double dStt = (Double) data.get("status");
                    Double dWidth = (Double) data.get("width");
                    Double dHeight = (Double) data.get("height");
                    Double dRange = (Double) data.get("range");
                    Double dResolution = (Double) data.get("resolution");
                    Double dWeight = (Double) data.get("weight");

                    request.setQrcode(s);
                    request.setName((String) data.get("name"));
                    request.setStatus(dStt.intValue());
                    request.setWidth(dWidth.floatValue());
                    request.setHeight(dHeight.floatValue());
                    request.setRange(dRange.floatValue());
                    request.setResolution(dResolution.floatValue());
                    request.setWeight(dWeight.floatValue());
                    request.setDescription((String) data.get("description"));
                    request.setLocation((String) data.get("location"));
                    Equipment rs = equipmentService.handleSave(request, category);
                    if (rs != null) {
                        images.forEach(img -> imageService.handleSave(rs, img));
                        count.incrementAndGet();
                    }

                    if (count.get() % 10 == 0) {
//                        log.info("Created: {}", count.get());
                        bulkEquipmentLog.setCreated(count.get());
                        repository.save(bulkEquipmentLog);
                    }
                }
                bulkEquipmentLog.setStatus(1);
                bulkEquipmentLog.setCreated(count.get());
                bulkEquipmentLog.setCompletedAt(new Timestamp(System.currentTimeMillis()));
                repository.save(bulkEquipmentLog);
//                log.info("Successful: {}/{}", count.get(), bulkEquipmentLog.getQuantity());
                return true;
            }
        } catch (Exception e) {
            log.error("Error BulkEquipmentService: {}", e.getMessage());
            return false;
        }
    }
}
