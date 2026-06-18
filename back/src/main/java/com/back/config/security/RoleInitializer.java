package com.back.config.security;

import com.back.entity.po.Role;
import com.back.entity.po.User;
import com.back.repository.RoleRepository;
import com.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 启动时初始化角色 (user / admin / root)，并在 lc_user 为空时创建一个 root 账户便于登录调试。
 * 与 lowcode {@code DataInitializer} 解耦，跑在更早的阶段（{@link Order} 较小）。
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    /** 默认 root 账号，仅在用户表为空时创建 */
    private static final String DEFAULT_ROOT_USERNAME = "root";
    private static final String DEFAULT_ROOT_PASSWORD = "root123";
    private static final String DEFAULT_ROOT_NICKNAME = "超级管理员";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initRootUser();
    }

    private void initRoles() {
        // 用 LinkedHashMap 保证插入顺序，便于排查
        Map<String, String> roleSpecs = new LinkedHashMap<>();
        roleSpecs.put("user", "普通用户");
        roleSpecs.put("admin", "管理员");
        roleSpecs.put("root", "超级管理员");

        roleSpecs.forEach((code, name) -> {
            if (!roleRepository.existsByCode(code)) {
                roleRepository.save(Role.builder().code(code).name(name).build());
                log.info("初始化角色: {} ({})", code, name);
            }
        });
    }

    private void initRootUser() {
        if (userRepository.count() > 0) {
            return;
        }
        Optional<Role> rootRole = roleRepository.findByCode("root");
        if (rootRole.isEmpty()) {
            log.warn("root 角色缺失，跳过 root 用户初始化");
            return;
        }
        Set<Role> roles = new HashSet<>(List.of(rootRole.get()));
        User root = User.builder()
                .username(DEFAULT_ROOT_USERNAME)
                .password(passwordEncoder.encode(DEFAULT_ROOT_PASSWORD))
                .nickname(DEFAULT_ROOT_NICKNAME)
                .roles(roles)
                .build();
        userRepository.save(root);
        log.info("初始化 root 账户: username={} password={}（请尽快修改密码）",
                DEFAULT_ROOT_USERNAME, DEFAULT_ROOT_PASSWORD);
    }
}
