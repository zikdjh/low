package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DDL 执行日志 — 审计所有动态 DDL 操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_ddl_log")
public class DdlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联实体 ID */
    @Column(nullable = false)
    private Long entityId;

    /** 执行的 SQL 语句 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sqlStatement;

    /** 操作类型: CREATE_TABLE, ALTER_TABLE, DROP_TABLE */
    @Column(nullable = false, length = 32)
    private String operationType;

    /** 执行结果: SUCCESS, FAILED */
    @Column(name = "exec_result", nullable = false, length = 16)
    private String result;

    /** 错误信息（如果有） */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        this.executedAt = LocalDateTime.now();
    }
}
