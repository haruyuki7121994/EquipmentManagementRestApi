package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Comment;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> getByUser(User user, Pageable pageable);
}
