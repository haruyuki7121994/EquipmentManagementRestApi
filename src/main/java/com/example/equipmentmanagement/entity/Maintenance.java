package com.example.equipmentmanagement.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "maintenances")
public class Maintenance {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Column(name = "date_maintenance", nullable = false)
    private Date dateMaintenance;

    @Column(name = "lastdate_maintenance", nullable = true)
    private Date lastdateMaintenance;

    @Column(name = "status", nullable = true)
    private int status;

    @Column(name = "repeatable", nullable = true)
    private Boolean repeatable;

    @Column(name = "repeated_type", nullable = true)
    private int repeatedType;

    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy="maintenance")
    private Set<Equipment> equipments;

    @OneToMany(mappedBy="maintenance")
    private Set<Notification> notifications;
}
