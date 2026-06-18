package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.UserAccount;
import com.back.lowcode.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/code/phone")
    public Result sendPhoneCode(@RequestParam String phone) {
        String code = userService.sendPhoneCode(phone);
        Map<String, String> response = new HashMap<>();
        response.put("message", "验证码已发送");
        response.put("code", code);
        return Result.success(response);
    }

    @PostMapping("/user/login")
    public Result login(@RequestParam String phone, @RequestParam String code, HttpServletResponse response) {
        String accessToken = userService.login(phone, code);
        UserAccount user = userService.getUserFromAccessToken(accessToken);
        String refreshToken = userService.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return Result.success(accessToken);
    }

    @PatchMapping("/user/refresh/access")
    public Result refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie refreshCookie = WebUtils.getCookie(request, "refreshToken");
        if (refreshCookie == null) {
            throw new IllegalArgumentException("刷新令牌不存在");
        }
        String newAccess = userService.refreshAccessToken(refreshCookie.getValue());
        return Result.success(newAccess);
    }

    @GetMapping("/user/info/personal")
    public Result getUserInfoPersonal(@RequestHeader(name = "access", required = false) String accessToken) {
        UserAccount user = userService.getUserFromAccessToken(accessToken);
        Map<String, Object> data = new HashMap<>();
        data.put("phone", user.getPhone());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        return Result.success(data);
    }
}