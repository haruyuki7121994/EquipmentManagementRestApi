package com.example.equipmentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "equipments")
public class Equipment {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Basic
    @Column(name = "qrcode", nullable = false, length = 255)
    private String qrcode;

    @Basic
    @Column(name = "description", nullable = true)
    private String description;

    @Basic
    @Column(name = "location", nullable = true)
    private String location;

    @Basic
    @Column(name = "status", nullable = true)
    private Integer status;

    @Basic
    @Column(name = "width", nullable = true, precision = 0)
    private Float width;

    @Basic
    @Column(name = "height", nullable = true, precision = 0)
    private Float height;

    @Basic
    @Column(name = "range", nullable = true, precision = 0)
    private Float range;

    @Basic
    @Column(name = "resolution", nullable = true, precision = 0)
    private Float resolution;

    @Basic
    @Column(name = "weight", nullable = true, precision = 0)
    private Float weight;

    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;

    @Basic
    @Column(name = "lastdate_maintenance", nullable = true)
    private Date lastdateMaintenance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
//    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy="equipment")
    private Set<Image> images;

    @OneToMany(mappedBy="equipment")
    private Set<Comment> comments;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maintenance_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    @ToString.Exclude
    private Maintenance maintenance;

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
