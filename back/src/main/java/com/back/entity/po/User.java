package com.back.entity.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 平台用户 — 用于登录 / 注册。
 * 沿用项目 lc_ 前缀约定，密码使用 BCrypt 加密存储。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录用户名，唯一 */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** BCrypt 加密后的密码（密文长度通常 60 位，留足余量） */
    @Column(nullable = false, length = 100)
    private String password;

    /** 显示昵称（可选） */
    @Column(length = 64)
    private String nickname;

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
