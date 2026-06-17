package com.back.service.impl;

import com.back.common.Result;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;
import com.back.entity.po.User;
import com.back.repository.UserRepository;
import com.back.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Result register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return Result.error("用户名已存在");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .build();
        User saved = userRepository.save(user);
        log.info("注册新用户 id={} username={}", saved.getId(), saved.getUsername());
        return Result.success(toView(saved));
    }

    @Override
    public Result login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(toView(user));
    }

    /** 移除密码字段后再返回 */
    private Map<String, Object> toView(User user) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("nickname", user.getNickname());
        view.put("createdAt", user.getCreatedAt());
        return view;
    }
}
