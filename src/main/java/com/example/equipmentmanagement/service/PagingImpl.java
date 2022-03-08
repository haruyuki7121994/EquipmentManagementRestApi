package com.example.equipmentmanagement.service;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface PagingImpl {
    Map<String, Object> getMetadata(Page<?> objectPage);
}
