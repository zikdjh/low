package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lc_page_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_code", unique = true, nullable = false, length = 64)
    private String pageCode;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "layout_json", columnDefinition = "LONGTEXT")
    private String layoutJson;

    @Column(name = "page_type", length = 32)
    private String pageType = "list";

    @Column(name = "entity_code", length = 64)
    private String entityCode;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "status")
    private String status = "draft";

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

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