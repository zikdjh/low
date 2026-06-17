package com.back.service;

import com.back.common.Result;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;

public interface AuthService {

    /** 注册：用户名唯一，密码 BCrypt 加密后落库 */
    Result register(RegisterRequest req);

    /** 登录：校验用户名 + 密码 */
    Result login(LoginRequest req);
}
