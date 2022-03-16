package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.CommentRequest;
import com.example.equipmentmanagement.dto.MessageResponse;
import com.example.equipmentmanagement.dto.PagingMessageResponse;
import com.example.equipmentmanagement.entity.*;
import com.example.equipmentmanagement.repository.CommentRepository;
import com.example.equipmentmanagement.repository.EquipmentRepository;
import com.example.equipmentmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    CommentRepository repository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/all")
    public ResponseEntity<?> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id-desc") String orderBy,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String date
    ) {
        try {
            List<Comment> comments;

            String[] parts = orderBy.split("-");
            Pageable paging = PageRequest.of(
                    page, size,
                    parts[1].equals("desc") ? Sort.by(parts[0]).descending() : Sort.by(parts[0]).ascending()
            );

            Page<Comment> pageComments;
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                pageComments = repository.findAll(paging);
            } else if (date.equals("last-week")) {
                LocalDateTime localDateTime = LocalDateTime.now().minusDays(7);
                Timestamp lastWeek = Timestamp.valueOf(localDateTime);
                pageComments = repository.findAllByCreatedAtGreaterThanEqual(lastWeek, paging);
            }
            else {
                User user = userOptional.get();
                pageComments = repository.getByUser(user, paging);
            }

            comments = pageComments.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentPage", pageComments.getNumber());
            metadata.put("totalItems", pageComments.getTotalElements());
            metadata.put("totalPages", pageComments.getTotalPages());
            metadata.put("size", pageComments.getSize());

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
        Optional<Comment> commentOpt = repository.findById(id);
        if (commentOpt.isPresent()) {
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Find successful!",
                            commentOpt.get()
                    )
            );
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CommentRequest commentRequest) {
        Optional<Equipment> equipmentOptional = equipmentRepository.findById(commentRequest.getEquipment_id());
        Equipment equipment;
        if (equipmentOptional.isPresent()) {
            equipment = equipmentOptional.get();
        }
        else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Equipment not found!",
                            null
                    )
            );
        }

        Optional<User> userOptional = userRepository.findByUsernameAndRolesNotContainGuest(commentRequest.getUser_id());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        }
        else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "User not found!",
                            null
                    )
            );
        }

        try {
            Comment comment = new Comment();
            comment.setId(new Timestamp(System.currentTimeMillis()).getTime() + "cmt-");
            comment.setTitle(commentRequest.getTitle());
            comment.setDescription(commentRequest.getDescription());
            comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            comment.setEquipment(equipment);
            comment.setUser(user);
            repository.save(comment);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Create successful!",
                            comment
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
