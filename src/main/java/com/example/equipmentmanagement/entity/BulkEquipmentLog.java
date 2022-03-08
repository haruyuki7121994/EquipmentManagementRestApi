package com.example.equipmentmanagement.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bulk_equipment_logs")
public class BulkEquipmentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Basic
    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Basic
    @Column(name = "created", nullable = false)
    private int created;

    @Basic
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Basic
    @Column(name = "image_paths", nullable = true, length = -1)
    private String imagePaths;

    @Basic
    @Column(name = "qrcode_list", nullable = true, length = -1)
    private String qrcodeList;

    @Basic
    @Column(name = "data", nullable = false, length = -1)
    private String data;

    @Basic
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Basic
    @Column(name = "completed_at", nullable = true)
    private Timestamp completedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulkEquipmentLog that = (BulkEquipmentLog) o;
        return id == that.id && quantity == that.quantity && created == that.created && Objects.equals(status, that.status) && Objects.equals(imagePaths, that.imagePaths) && Objects.equals(data, that.data) && Objects.equals(createdAt, that.createdAt) && Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity, created, status, imagePaths, data, createdAt, completedAt);
    }
}
