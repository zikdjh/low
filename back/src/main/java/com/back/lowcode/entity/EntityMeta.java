package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实体元数据 — 用户定义的业务实体（如"客户"、"订单"）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_entity_meta")
public class EntityMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 实体编码，唯一标识，如 "customer"、"order" */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /** 实体显示名称，如 "客户"、"订单" */
    @Column(name = "entity_name", nullable = false, length = 128)
    private String name;

    /** 实际数据库表名，由 code 加 lc_ 前缀生成 */
    @Column(nullable = false, unique = true, length = 128)
    private String tableName;

    /** 实体描述 */
    @Column(length = 512)
    private String description;

    /**
     * 状态：
     * draft — 仅元数据存在，未创建物理表
     * published — 已创建物理表，可进行 CRUD
     * archived — 已归档，不可操作
     */
    @Column(name = "entity_status", nullable = false, length = 16)
    @Builder.Default
    private String status = "draft";

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
