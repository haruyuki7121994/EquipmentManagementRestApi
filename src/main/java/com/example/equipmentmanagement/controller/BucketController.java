package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.aws.AmazonClient;
import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/storage/")
public class BucketController {
    private final AmazonClient amazonClient;

    @Autowired
    ImageRepository repository;

    @Autowired
    BucketController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestPart(value = "file") MultipartFile file) {
        return ResponseEntity.ok().body(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        "Upload successful!",
                        this.amazonClient.uploadFile(file)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        try {
            repository.deleteById(id);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Delete successful!",
                            null
                    )
            );
        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
