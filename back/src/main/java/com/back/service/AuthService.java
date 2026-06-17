package com.back.service;

import com.back.common.Result;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * 注册：用户名唯一，默认绑定 user 角色，密码 BCrypt 加密；
     * 注册成功直接签发 token（注册即登录），refresh token 写入 HttpOnly Cookie。
     */
    Result register(RegisterRequest req, HttpServletResponse response);

    /**
     * 登录：走 Spring Security AuthenticationManager 验证用户名/密码；
     * 成功签发 access + refresh，refresh 写入 HttpOnly Cookie。
     */
    Result login(LoginRequest req, HttpServletResponse response);

    /**
     * 刷新 access：从 HttpOnly Cookie 读取 refresh，校验后重新签发 access。
     */
    Result refresh(HttpServletRequest request, HttpServletResponse response);

    /**
     * 退出登录：清空 refresh Cookie；access 由前端自行丢弃即可（无服务端黑名单）。
     */
    Result logout(HttpServletResponse response);
}
