package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Comment;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface CommentRepository extends JpaRepository<Comment, String> {
    Page<Comment> getByUser(User user, Pageable pageable);
    Page<Comment> findAllByCreatedAtGreaterThanEqual(Timestamp createdAt, Pageable pageable);
}
