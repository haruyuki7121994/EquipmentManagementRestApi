package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CategoryRequest;
import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.dto.PagingMessageResponse;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    CategoryRepository repository;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {
            List<Category> categories;
            Pageable paging = PageRequest.of(page, size);
            Page<Category> pageCates = repository.findAll(paging);
            categories = pageCates.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentPage", pageCates.getNumber());
            metadata.put("totalItems", pageCates.getTotalElements());
            metadata.put("totalPages", pageCates.getTotalPages());

            return ResponseEntity.ok(
                    new PagingMessageResponse(
                            HttpStatus.OK.value(),
                            metadata,
                            response
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable("id") String id) {
        Optional<Category> categoryOpt = repository.findById(id);
        if (categoryOpt.isPresent()) {
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Find successful!",
                            categoryOpt.get()
                    )
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest categoryRequest) {
        try {
            Category cate = new Category();
            cate.setId("cate-" + new Timestamp(System.currentTimeMillis()).getTime());
            cate.setName(categoryRequest.getName());
            cate.setActive(categoryRequest.getActive());
            repository.save(cate);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Create successful!",
                            cate
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody CategoryRequest categoryRequest) {
        Optional<Category> cateOpt = repository.findById(id);
        if (cateOpt.isPresent()) {
            Category category = cateOpt.get();
            category.setName(categoryRequest.getName());
            category.setActive(categoryRequest.getActive());
            repository.save(category);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Update successful!",
                            category
                    )
            );
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
