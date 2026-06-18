package com.back.interceptors;

import com.back.common.Result;
import com.back.lowcode.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOW_PATHS = List.of(
            "/user/code/phone",
            "/user/login",
            "/user/refresh/access"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api")) {
            // 前端请求会经过 /api 代理到后端，实际后端 URI 不包含 /api
            path = path.substring(4);
        }
        if (path.startsWith("/user") && ALLOW_PATHS.contains(path)) {
            return true;
        }
        if (path.startsWith("/user") && path.equals("/user/info/personal")) {
            return validateAccess(request, response);
        }
        if (path.startsWith("/lowcode") || path.startsWith("/user")) {
            return validateAccess(request, response);
        }
        return true;
    }

    private boolean validateAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getHeader("access");
        if (!StringUtils.hasText(accessToken)) {
            writeUnauthorized(response, "请先登录");
            return false;
        }
        try {
            userService.getUserFromAccessToken(accessToken);
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, e.getMessage());
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(499);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(message)));
    }
}
