package com.example.equipmentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String password;
    private String rePassword;
    private String avatar;
    private Boolean active;
    private Set<String> role;
}
