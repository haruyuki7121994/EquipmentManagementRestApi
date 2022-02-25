package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {
}
