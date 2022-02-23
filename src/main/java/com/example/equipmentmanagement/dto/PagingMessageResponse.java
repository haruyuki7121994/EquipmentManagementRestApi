package com.example.equipmentmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class PagingMessageResponse {
    private int status;
    private Map<String, Object> metadata;
    private Map<String, Object> data;
}
