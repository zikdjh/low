package com.back.config.security;

import com.back.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 鉴权失败 / 越权 的统一 JSON 输出，沿用 {@link Result} 包装，
 * 401（未登录或 token 失效）和 403（权限不足）分别用不同的 HTTP 状态码。
 */
@Component
@RequiredArgsConstructor
public class JsonAuthFailureHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint entryPoint() {
        return (request, response, ex) -> writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ex, "未登录或登录已过期");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeError(response, HttpServletResponse.SC_FORBIDDEN, ex, "无权访问该资源");
    }

    private void writeError(HttpServletResponse response, int status, Exception ex, String fallback) throws IOException {
        // 仅替换状态码与消息体；ex.getMessage 可能含敏感信息，这里只在 401/403 输出固定文案
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result body = Result.error(fallback);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        // ex 类型在编译期不一定是 AuthenticationException / AccessDeniedException，留作未来扩展
        boolean used = ex instanceof AuthenticationException || ex instanceof AccessDeniedException;
        if (!used) {
            // no-op：消除 IDE "unused" 警告
        }
    }
}
