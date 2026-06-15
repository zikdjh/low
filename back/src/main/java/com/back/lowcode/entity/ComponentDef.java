package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lc_component_def")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comp_key", unique = true, nullable = false, length = 64)
    private String compKey;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "icon", length = 128)
    private String icon;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "default_props_json", columnDefinition = "TEXT")
    private String defaultPropsJson;

    @Column(name = "props_schema_json", columnDefinition = "TEXT")
    private String propsSchemaJson;

    @Column(name = "group_index")
    private Integer groupIndex;

    @Column(name = "is_system")
    private Boolean isSystem = true;

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