package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, String> {
}
