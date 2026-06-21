package com.back.controller;

import com.back.common.Result;
import com.back.config.security.AuthUserPrincipal;
import com.back.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员端 Controller — 用户管理 / 页面管理 / 实体管理 / 个人信息。
 * 所有接口要求 admin 或 root 角色（由 SecurityConfig 控制）。
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---- 用户管理 ----

    /** 获取所有用户 */
    @GetMapping("/users")
    public Result listUsers() {
        return adminService.listUsers();
    }

    /** 冻结用户 */
    @PutMapping("/users/{id}/freeze")
    public Result freezeUser(@PathVariable Long id) {
        return adminService.freezeUser(id);
    }

    /** 解冻用户 */
    @PutMapping("/users/{id}/unfreeze")
    public Result unfreezeUser(@PathVariable Long id) {
        return adminService.unfreezeUser(id);
    }

    // ---- 页面管理 ----

    /** 获取所有页面（所有用户） */
    @GetMapping("/pages")
    public Result listAllPages() {
        return adminService.listAllPages();
    }

    // ---- 实体管理 ----

    /** 获取所有实体（所有用户） */
    @GetMapping("/entities")
    public Result listAllEntities() {
        return adminService.listAllEntities();
    }

    // ---- 个人信息 ----

    /** 获取管理员个人信息 */
    @GetMapping("/profile")
    public Result getProfile(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return adminService.getProfile(principal.getUid());
    }

    /** 更新管理员个人信息 */
    @PutMapping("/profile")
    public Result updateProfile(@AuthenticationPrincipal AuthUserPrincipal principal,
                                @RequestBody Map<String, String> body) {
        return adminService.updateProfile(principal.getUid(), body);
    }
}
