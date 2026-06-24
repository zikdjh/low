package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 应用菜单（lc_app_menu）
 * <p>
 * 同一行的语义由 {@code releaseId} 决定：
 * <ul>
 *   <li>{@code releaseId IS NULL} —— 设计器草稿态，{@link com.back.lowcode.lcmenu.MenuService} 进行 CRUD/拖拽</li>
 *   <li>{@code releaseId != null} —— release 快照态，由 {@code MountPhase} 在发布流水线中从草稿克隆而来，
 *       属于不可变历史</li>
 * </ul>
 * 终端用户经 {@code GET /lowcode/app/{appCode}/menu} 仅看到当前 active release 的快照行。
 * <p>
 * 自引用 {@code parentId} 形成菜单树；根节点 {@code parentId} 为 {@code null}。同层按 {@code sortOrder} 升序展示。
 */
@Entity
@Table(name = "lc_app_menu", indexes = {
        @Index(name = "idx_app_release", columnList = "app_code,release_id"),
        @Index(name = "idx_parent", columnList = "parent_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_code", nullable = false, length = 64)
    private String appCode;

    /** null = 草稿；非空 = 该 release 的菜单快照 */
    @Column(name = "release_id")
    private Long releaseId;

    /** 自引用：父菜单 id；根节点为 null */
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "icon", length = 64)
    private String icon;

    /** 同层排序，升序 */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /** 业务页面 code（指向 lc_page_schema.page_code），叶子菜单必填 */
    @Column(name = "page_code", length = 64)
    private String pageCode;

    /** 运行时路由路径，可为空（前端会基于 appCode + pageCode 兜底拼接） */
    @Column(name = "route_path", length = 256)
    private String routePath;

    /** 菜单类型：menu 普通菜单 / group 分组（无 pageCode）/ external 外链 */
    @Column(name = "menu_type", length = 16)
    @Builder.Default
    private String menuType = "menu";

    /** 是否可见，隐藏的仅作权限占位 */
    @Column(name = "visible", nullable = false)
    @Builder.Default
    private Boolean visible = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
