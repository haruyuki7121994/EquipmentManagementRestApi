package com.example.equipmentmanagement.repository;

import com.example.equipmentmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
}
