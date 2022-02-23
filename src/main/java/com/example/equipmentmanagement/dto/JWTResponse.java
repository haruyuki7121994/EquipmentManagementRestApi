package com.example.equipmentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String username;
    private List<String> roles;
}
