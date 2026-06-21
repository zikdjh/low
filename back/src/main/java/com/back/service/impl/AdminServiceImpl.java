package com.back.service.impl;

import com.back.common.Result;
import com.back.entity.po.Role;
import com.back.entity.po.User;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.PageSchemaRepository;
import com.back.repository.UserRepository;
import com.back.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PageSchemaRepository pageSchemaRepository;
    private final EntityMetaRepository entityMetaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Result listUsers() {
        List<User> users = userRepository.findAll();
        log.info("管理员查询用户列表，共 {} 条", users.size());
        List<Map<String, Object>> list = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("status", u.getStatus());
            m.put("roles", u.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
            m.put("createdAt", u.getCreatedAt());
            m.put("updatedAt", u.getUpdatedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result freezeUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error("用户不存在");
        if ("frozen".equals(user.getStatus())) return Result.error("该用户已处于冻结状态");
        user.setStatus("frozen");
        userRepository.save(user);
        log.info("管理员冻结用户 id={} username={}", user.getId(), user.getUsername());
        return Result.success();
    }

    @Override
    @Transactional
    public Result unfreezeUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error("用户不存在");
        if ("active".equals(user.getStatus())) return Result.error("该用户已处于活跃状态");
        user.setStatus("active");
        userRepository.save(user);
        log.info("管理员解冻用户 id={} username={}", user.getId(), user.getUsername());
        return Result.success();
    }

    @Override
    @Transactional(readOnly = true)
    public Result listAllPages() {
        List<PageSchema> pages = pageSchemaRepository.findAll();
        log.info("管理员查询页面列表，共 {} 条", pages.size());
        return Result.success(pages);
    }

    @Override
    @Transactional(readOnly = true)
    public Result listAllEntities() {
        List<EntityMeta> entities = entityMetaRepository.findAll();
        log.info("管理员查询实体列表，共 {} 条", entities.size());
        return Result.success(entities);
    }

    @Override
    public Result getProfile(Long adminId) {
        User user = userRepository.findById(adminId).orElse(null);
        if (user == null) return Result.error("管理员不存在");
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("roles", user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
        profile.put("createdAt", user.getCreatedAt());
        return Result.success(profile);
    }

    @Override
    @Transactional
    public Result updateProfile(Long adminId, Map<String, String> body) {
        User user = userRepository.findById(adminId).orElse(null);
        if (user == null) return Result.error("管理员不存在");

        if (body.containsKey("nickname")) {
            user.setNickname(body.get("nickname"));
        }
        if (body.containsKey("password") && body.get("password") != null && !body.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(body.get("password")));
        }
        userRepository.save(user);
        log.info("管理员更新个人信息 id={} username={}", user.getId(), user.getUsername());
        return Result.success();
    }
}
