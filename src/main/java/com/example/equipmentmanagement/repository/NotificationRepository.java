package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query(value = "select n.* from notifications n join maintenances m on n.maintenance_id = m.id join users u on u.id = m.user_id where u.username = :username", nativeQuery = true)
    List<Notification> getByUsername(@Param("username") String username);
}
