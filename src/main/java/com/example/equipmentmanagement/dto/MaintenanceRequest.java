package com.example.equipmentmanagement.dto;

import com.example.equipmentmanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceRequest {
    private String id;
    private Date dateMaintenance;
    private Date lastdateMaintenance;
    private int status;
    private Boolean repeatable;
    private int repeatedType;
    private String maintainer;
    private String createdAt;
    private User user;
    private Set<String> equipmentListQrcode;
}
