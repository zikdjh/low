package com.back.entity.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色实体（user / admin / root）。
 * 三级权限体系第二层；与 {@link User} 多对多，
 * 与 {@link Permission} 的关联本期暂不建立（按需求"暂不设置权限"留空）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色编码（user / admin / root），唯一 */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** 显示名称 */
    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 255)
    private String description;
}
