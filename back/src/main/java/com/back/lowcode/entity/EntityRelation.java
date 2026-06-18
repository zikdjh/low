package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_entity_relation")
public class EntityRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_entity_code", nullable = false, length = 64)
    private String sourceEntityCode;

    @Column(name = "source_field_code", nullable = false, length = 64)
    private String sourceFieldCode;

    @Column(name = "target_entity_code", nullable = false, length = 64)
    private String targetEntityCode;

    @Column(name = "target_display_field_code", length = 64)
    private String targetDisplayFieldCode;

    @Column(name = "relation_type", nullable = false, length = 16)
    private String relationType;

    @Column(length = 256)
    private String description;

    @Builder.Default
    private Boolean cascadeDelete = false;

    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}