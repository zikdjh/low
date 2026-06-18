package com.back.controller;

import com.back.common.Result;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;
import com.back.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller — 注册 / 登录 / 刷新 / 登出。
 * 所有接口均通过 SecurityConfig 配置为公开访问，鉴权完全依赖 JWT。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册：注册即登录，返回 access token + user，refresh 写入 HttpOnly Cookie */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest req, HttpServletResponse response) {
        return authService.register(req, response);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        return authService.login(req, response);
    }

    /** 刷新 access token：从 HttpOnly Cookie 读取 refresh token */
    @PostMapping("/refresh")
    public Result refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    /** 登出：清空 refresh Cookie */
    @PostMapping("/logout")
    public Result logout(HttpServletResponse response) {
        return authService.logout(response);
    }
}
