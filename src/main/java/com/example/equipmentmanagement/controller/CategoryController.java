package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CategoryRequest;
import com.example.equipmentmanagement.entity.Category;
import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.repository.CategoryRepository;
import com.example.equipmentmanagement.service.PagingImpl;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    CategoryRepository repository;
    @Autowired
    PagingImpl pagingService;
    @Autowired
    ResponseImpl responseService;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "-1") int active,
            @RequestParam(defaultValue = "") String keyword
    ) {
        try {
            List<Category> categories;
            String[] parts = orderBy.split("-");

            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );

            Page<Category> pageCates;
            if (active == 0 || active == 1) {
                pageCates = repository.getByIsActive(active != 0, paging);
            } else if (keyword.isEmpty()) {
                pageCates = repository.findAll(paging);
            } else {
                pageCates = repository.getByNameContains(keyword, paging);
            }

            categories = pageCates.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);

            Map<String, Object> metadata = pagingService.getMetadata(pageCates);

            return responseService.successWithPaging(metadata, response);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable("id") String id) {
        Optional<Category> categoryOpt = repository.findById(id);
        if (categoryOpt.isPresent()) {
            return responseService.success("Find successful!", categoryOpt.get());
        }
        return responseService.notFound();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest categoryRequest) {
        if (repository.existsByName(categoryRequest.getName())) {
            return responseService.alreadyExists("Name is exist!");
        }
        try {
            Category cate = new Category();
            cate.setId(new Timestamp(System.currentTimeMillis()).getTime() + "-cate");
            cate.setName(categoryRequest.getName());
            cate.setIsActive(categoryRequest.getActive());
            repository.save(cate);
            return responseService.success("Create successful!", cate);
        } catch (Exception e) {
            return responseService.serverError();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody CategoryRequest categoryRequest) {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        if (repository.existsByNameAndIdIsNotIn(categoryRequest.getName(), ids)) {
            return responseService.alreadyExists("Name is exist!");
        }
        Optional<Category> cateOpt = repository.findById(id);
        if (cateOpt.isPresent()) {
            Category category = cateOpt.get();
            category.setName(categoryRequest.getName());
            category.setIsActive(categoryRequest.getActive());
            repository.save(category);
            return responseService.success("Update successful!", category);
        } else {
            return responseService.serverError();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        try {
            Optional<Category> categoryOptional = repository.findById(id);

            if (categoryOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                Category category = categoryOptional.get();
                Set<Equipment> equipmentSet = category.getEquipments();
                if (equipmentSet.size() > 0) {
                    return responseService.badRequest("Cannot Delete!");
                }

                repository.deleteById(id);

                return responseService.success("Delete successful!", null);
            }
        } catch (Exception e) {
            return responseService.serverError();
        }
    }
}
