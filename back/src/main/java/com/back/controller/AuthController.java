package com.back.controller;

import com.back.common.Result;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;
import com.back.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller — 提供注册 / 登录。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册 */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
