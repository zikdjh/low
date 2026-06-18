package com.back.entity.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限实体（user-role-permission 三级权限的最末层）。
 * 本期暂不挂载到角色上，仅建表占位，后续按业务需要再补 role-permission 多对多。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 权限编码，唯一，如 entity:read / entity:publish */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    /** 显示名称 */
    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 255)
    private String description;
}
