package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lc_dict_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_code", nullable = false, length = 64)
    private String dictCode;

    @Column(name = "item_key", nullable = false, length = 128)
    private String itemKey;

    @Column(name = "item_value", nullable = false, length = 512)
    private String itemValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "color", length = 32)
    private String color;

    @Column(name = "status")
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}