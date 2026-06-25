package com.back.config.security;

import com.back.entity.po.Role;
import com.back.entity.po.User;
import com.back.repository.RoleRepository;
import com.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        initDemoUsers();
    }

    private void initRoles() {
        // 用 LinkedHashMap 保证插入顺序，便于排查
        Map<String, String> roleSpecs = new LinkedHashMap<>();
        roleSpecs.put("user", "普通用户");
        roleSpecs.put("admin", "管理员");
        roleSpecs.put("root", "超级管理员");
        // 请假管理场景专用角色
        roleSpecs.put("student", "学生");
        roleSpecs.put("counselor", "辅导员");
        roleSpecs.put("dept_head", "系主任");

        roleSpecs.forEach((code, name) -> {
            if (!roleRepository.existsByCode(code)) {
                roleRepository.save(Role.builder().code(code).name(name).build());
            }
        });
    }

    private void initDemoUsers() {
        // 初始化 root 账户（幂等）
        if (!userRepository.existsByUsername(DEFAULT_ROOT_USERNAME)) {
            Optional<Role> rootRole = roleRepository.findByCode("root");
            rootRole.ifPresent(role -> {
                Set<Role> roles = new HashSet<>(List.of(role));
                User root = User.builder()
                        .username(DEFAULT_ROOT_USERNAME)
                        .password(passwordEncoder.encode(DEFAULT_ROOT_PASSWORD))
                        .nickname(DEFAULT_ROOT_NICKNAME)
                        .roles(roles)
                        .build();
                userRepository.save(root);
            });
        }

        Role studentRole = roleRepository.findByCode("student").orElse(null);
        Role counselorRole = roleRepository.findByCode("counselor").orElse(null);
        Role deptHeadRole = roleRepository.findByCode("dept_head").orElse(null);
        Role adminRole = roleRepository.findByCode("admin").orElse(null);
        Role userRole = roleRepository.findByCode("user").orElse(null);

        // 初始化演示学生
        createDemoUser("zhangsan", "张三（学生）", Set.of(studentRole));
        createDemoUser("student01", "张三同学", Set.of(studentRole, userRole));
        createDemoUser("student02", "李四同学", Set.of(studentRole, userRole));

        // 初始化演示辅导员
        createDemoUser("fdy", "李辅导员", Set.of(counselorRole));
        createDemoUser("counselor01", "李辅导员", Set.of(counselorRole, userRole));

        // 初始化演示系主任
        createDemoUser("xizhuren", "王主任（系主任）", Set.of(deptHeadRole));
        createDemoUser("dept_head01", "王主任", Set.of(deptHeadRole, userRole));

        // 初始化演示管理员

        createDemoUser("admin01", "系统管理员", Set.of(adminRole, userRole));
    }

    private void createDemoUser(String username, String nickname, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return; // 已存在，幂等跳过
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("123456"))
                .nickname(nickname)
                .roles(roles)
                .build();
        userRepository.save(user);
    }
}
