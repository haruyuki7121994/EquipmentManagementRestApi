package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.*;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.CommentRepository;
import com.example.equipmentmanagement.repository.RoleRepository;
import com.example.equipmentmanagement.repository.UserRepository;
import com.example.equipmentmanagement.service.ResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserRepository repository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    ResponseImpl responseService;
    @Autowired
    CommentRepository commentRepository;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "all") String role,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String keyword
    ) {
        try {
            List<User> users;
            Page<User> pageUsers;
            String[] parts = orderBy.split("-");
            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );
            keyword = "%" + keyword + "%";
            switch (role) {
                case "maintainer":
                    pageUsers = repository.getByRole("ROLE_MAINTAINER", keyword, paging);
                    break;
                case "admin":
                    pageUsers = repository.getByRole("ROLE_ADMIN", keyword, paging);
                    break;
                case "guest":
                    pageUsers = repository.getByRole("ROLE_GUEST", keyword, paging);
                    break;
                default:
                    pageUsers = repository.findAll(paging);
                    break;
            }

            users = pageUsers.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentPage", pageUsers.getNumber());
            metadata.put("totalItems", pageUsers.getTotalElements());
            metadata.put("totalPages", pageUsers.getTotalPages());
            metadata.put("size", pageUsers.getSize());

            return ResponseEntity.ok(
                    new PagingMessageResponse(
                            HttpStatus.OK.value(),
                            metadata,
                            response
                    )
            );
        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> find(@PathVariable("id") String id) {
        Optional<User> userOpt = repository.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Find successful!",
                            userOpt.get()
                    )
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (repository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Error: Username is already taken!",
                                null
                        ));
            }
            if (repository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Error: Email is already in use!",
                                null
                        ));
            }

            // Create new user's account
            User user = new User();
            user.setId("user-" + new Timestamp(System.currentTimeMillis()).getTime());
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(encoder.encode(registerRequest.getPassword()));
            user.setAddress(registerRequest.getAddress());
            user.setPhone(registerRequest.getPhone());
            user.set_active(registerRequest.getActive());

            Set<String> strRoles = registerRequest.getRole();
            Set<Role> roles = new HashSet<>();
            strRoles.forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_MAINTAINER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            });
            user.setRoles(roles);
            repository.save(user);
            return ResponseEntity.ok(new MessageResponse(
                    HttpStatus.OK.value(),
                    "User registered successfully!",
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") String id, @Valid @RequestBody UserRequest userRequest) {
        Optional<User> useOpt = repository.findById(id);
        if (useOpt.isPresent()) {
            User user = useOpt.get();
            user.setPhone(userRequest.getPhone());
            user.setAddress(userRequest.getAddress());
            user.set_active(userRequest.getActive());
            repository.save(user);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Update successful!",
                            user
                    )
            );
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        Optional<User> userOptional = repository.findById(id);
        if (userOptional.isEmpty()) {
            return responseService.notFound();
        }

        try {
            User user = userOptional.get();

            Set<Maintenance> maintenances = user.getMaintenances();
            if (maintenances.size() > 0) return responseService.badRequest("Delete failed!");

            Set<Comment> comments = user.getComments();
            if (comments.size() > 0) commentRepository.deleteAll(comments);

            repository.delete(user);
            return responseService.success("Delete successful!", null);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
