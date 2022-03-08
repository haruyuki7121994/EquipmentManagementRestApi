package com.example.equipmentmanagement.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ResponseImpl {
    ResponseEntity<?> badRequest(String msg);
    ResponseEntity<?> success(String msg, Object data);
    ResponseEntity<?> successWithPaging(Map<String, Object> metadata, Map<String, Object> response);
    ResponseEntity<?> notFound();
    ResponseEntity<?> serverError();
    ResponseEntity<?> alreadyExists(String msg);
}
