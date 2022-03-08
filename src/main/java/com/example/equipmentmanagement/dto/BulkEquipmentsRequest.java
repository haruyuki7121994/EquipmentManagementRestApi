package com.example.equipmentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkEquipmentsRequest {
    private Integer quantity;
    private String type_qrcode;
    private String prefix;
    private Set<String> manual_qrcode;
    private String name;
    private Integer status;
    private Float width;
    private Float height;
    private Float range;
    private Float resolution;
    private Float weight;
    private String category_id;
    private String description;
    private String location;
    private MultipartFile[] images;
}
