package com.example.equipmentmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "equipments")
public class Equipment {
    private String id;
    private String name;
    private String qrcode;
    private Integer status;
    private Float width;
    private Float height;
    private Float range;
    private Float resolution;
    private Float weight;
    private Date createdAt;
    private Date lastdateMaintenance;

    @Id
    @Column(name = "id", nullable = false, length = 100)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "qrcode", nullable = false, length = 255)
    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    @Basic
    @Column(name = "status", nullable = true)
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "width", nullable = true, precision = 0)
    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    @Basic
    @Column(name = "height", nullable = true, precision = 0)
    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    @Basic
    @Column(name = "range", nullable = true, precision = 0)
    public Float getRange() {
        return range;
    }

    public void setRange(Float range) {
        this.range = range;
    }

    @Basic
    @Column(name = "resolution", nullable = true, precision = 0)
    public Float getResolution() {
        return resolution;
    }

    public void setResolution(Float resolution) {
        this.resolution = resolution;
    }

    @Basic
    @Column(name = "weight", nullable = true, precision = 0)
    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    @Basic
    @Column(name = "created_at", nullable = true)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Basic
    @Column(name = "lastdate_maintenance", nullable = true)
    public Date getLastdateMaintenance() {
        return lastdateMaintenance;
    }

    public void setLastdateMaintenance(Date lastdateMaintenance) {
        this.lastdateMaintenance = lastdateMaintenance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(id, equipment.id) && Objects.equals(name, equipment.name) && Objects.equals(qrcode, equipment.qrcode) && Objects.equals(status, equipment.status) && Objects.equals(width, equipment.width) && Objects.equals(height, equipment.height) && Objects.equals(range, equipment.range) && Objects.equals(resolution, equipment.resolution) && Objects.equals(weight, equipment.weight) && Objects.equals(createdAt, equipment.createdAt) && Objects.equals(lastdateMaintenance, equipment.lastdateMaintenance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, qrcode, status, width, height, range, resolution, weight, createdAt, lastdateMaintenance);
    }
}
