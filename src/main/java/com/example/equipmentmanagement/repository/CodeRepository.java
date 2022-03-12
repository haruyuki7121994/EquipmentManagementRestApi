package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Code;
import com.example.equipmentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Integer> {
    Optional<Code> findTopByUserAndCodeAndUsedIsFalseOrderByIdDesc(User user, String code);
}
