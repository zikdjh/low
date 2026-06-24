package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 发布流水线阶段审计 — 一行一个 phase 的执行结果
 */
@Entity
@Table(name = "lc_release_log",
        indexes = @Index(name = "idx_release", columnList = "release_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_id", nullable = false)
    private Long releaseId;

    /** 阶段：validate | snapshot | generate | mount | activate */
    @Column(name = "phase", nullable = false, length = 32)
    private String phase;

    /** 结果：success | failure | skipped */
    @Column(name = "result", nullable = false, length = 16)
    private String result;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
