package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 应用级发布单 — 一次"应用发布"的根记录
 * <p>
 * 与 PageSchema 草稿态状态翻转不同，Release 是不可变的发布快照根：
 * 一旦 status=active，其引用的 ReleaseItem 内容必须保持不变。
 * 回滚通过原子切换 active 标记完成，不修改历史 Release。
 */
@Entity
@Table(name = "lc_release",
        uniqueConstraints = @UniqueConstraint(name = "uk_app_version", columnNames = {"app_code", "version"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 业务应用 code，对应 BusinessApp.code */
    @Column(name = "app_code", nullable = false, length = 64)
    private String appCode;

    /** 语义化版本号 1.2.0 */
    @Column(name = "version", nullable = false, length = 32)
    private String version;

    /**
     * 状态：
     * draft        — 流水线已创建但未激活（灰度态或失败后保留）
     * active       — 当前线上版本，运行时只读这个
     * rolledback   — 曾经 active，被回滚或被新版本替换
     * failed       — 流水线执行失败
     */
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private String status = "draft";

    /** 发布说明 */
    @Column(name = "notes", length = 1000)
    private String notes;

    /** 全部 ReleaseItem checksum 的汇总 sha256，便于发布去重 */
    @Column(name = "checksum", length = 64)
    private String checksum;

    /** 发布人 */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 进入 active 状态的时间，回滚后不清零 */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
