package com.example.equipmentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Basic
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Basic
    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Basic
    @Column(name = "readed", nullable = true)
    private Boolean read;

    @Basic
    @Column(name = "created_at", nullable = false, length = 255)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maintenance_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @ToString.Exclude
    private Maintenance maintenance;
}
