package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.dto.PagingMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResponseService implements ResponseImpl {
    @Override
    public ResponseEntity<?> badRequest(String msg) {
        return ResponseEntity.badRequest().body(
                new MessageResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        msg,
                        null
                )
        );
    }

    @Override
    public ResponseEntity<?> success(String msg, Object data) {
        return ResponseEntity.ok(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        msg,
                        data
                )
        );
    }

    @Override
    public ResponseEntity<?> successWithPaging(Map<String, Object> metadata, Map<String, Object> response) {
        return ResponseEntity.ok(
                new PagingMessageResponse(
                        HttpStatus.OK.value(),
                        metadata,
                        response
                )
        );
    }


    @Override
    public ResponseEntity<?> notFound() {
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> serverError() {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> alreadyExists(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new MessageResponse(
                        HttpStatus.CONFLICT.value(),
                        msg,
                        null
                )
        );
    }
}
