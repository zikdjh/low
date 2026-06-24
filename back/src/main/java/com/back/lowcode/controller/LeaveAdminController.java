package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.entity.po.User;
import com.back.repository.UserRepository;
import com.back.lowcode.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 请假管理系统 — 管理员端控制器
 * 提供用户管理（冻结/解冻）和假条管理（查看/删除）
 */
@Slf4j
@RestController
@RequestMapping("/lowcode/leave/admin")
@RequiredArgsConstructor
public class LeaveAdminController {

    private final UserRepository userRepository;
    private final LeaveService leaveService;
    private final JdbcTemplate jdbcTemplate;

    // ==================== 用户管理 ====================

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer pageSize) {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", user.getId());
            m.put("username", user.getUsername());
            m.put("nickname", user.getNickname());
            m.put("status", user.getStatus());
            m.put("createdAt", user.getCreatedAt());
            m.put("roles", user.getRoles().stream().map(r -> {
                Map<String, Object> roleMap = new LinkedHashMap<>();
                roleMap.put("code", r.getCode());
                roleMap.put("name", r.getName());
                return roleMap;
            }).toList());
            return m;
        }).toList();
        return Result.success(result);
    }

    /**
     * 冻结用户
     */
    @PutMapping("/users/{id}/freeze")
    public Result freezeUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error("用户不存在");
        user.setStatus("frozen");
        userRepository.save(user);
        log.info("管理员冻结用户: id={}, username={}", id, user.getUsername());
        return Result.success("用户已冻结");
    }

    /**
     * 解冻用户
     */
    @PutMapping("/users/{id}/unfreeze")
    public Result unfreezeUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error("用户不存在");
        user.setStatus("active");
        userRepository.save(user);
        log.info("管理员解冻用户: id={}, username={}", id, user.getUsername());
        return Result.success("用户已解冻");
    }

    // ==================== 假条管理 ====================

    /**
     * 获取所有请假记录（管理员视图）
     */
    @GetMapping("/leaves")
    public Result getAllLeaves(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "20") Integer pageSize,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String keyword) {
        Map<String, String> filters = new HashMap<>();
        if (status != null && !status.isEmpty()) filters.put("status", status);
        Page<Map<String, Object>> result = leaveService.queryLeaveRecords(page, pageSize, null, status, null);
        return Result.success(result);
    }

    /**
     * 删除假条
     */
    @DeleteMapping("/leaves/{id}")
    public Result deleteLeave(@PathVariable Long id) {
        try {
            jdbcTemplate.update("DELETE FROM lc_leave_application WHERE id = ?", id);
            log.info("管理员删除假条: id={}", id);
            return Result.success("假条已删除");
        } catch (Exception e) {
            log.error("删除假条失败: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
