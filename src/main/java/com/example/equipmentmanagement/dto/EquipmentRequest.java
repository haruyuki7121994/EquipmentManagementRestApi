package com.example.equipmentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentRequest {
    private String name;
    private Integer status;
    private String qrcode;
    private Float width;
    private Float height;
    private Float range;
    private Float resolution;
    private Float weight;
    private String category_id;
    private String description;
    private String location;
    private MultipartFile[] images;
    private List<String> qrcodeList;
}
