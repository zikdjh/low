package com.back.config.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.common.Constant;
import com.back.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析请求头 {@code access} 中的 JWT，校验通过则填充 SecurityContext。
 *
 * <p>失败不直接抛 401，留给 {@code SecurityConfig} 配置的 AuthenticationEntryPoint 统一处理；
 * 这样匿名访问公开接口仍然走得通。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_ACCESS = Constant.ACCESS_TOKEN_TYPE;

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader(HEADER_ACCESS);
        if (StringUtils.hasText(token)) {
            DecodedJWT jwt = jwtUtil.verify(token, Constant.ACCESS_TOKEN_TYPE);
            if (jwt != null) {
                Long uid = jwtUtil.getUid(jwt);
                String username = jwtUtil.getUsername(jwt);
                List<String> roles = jwtUtil.getRoles(jwt);
                if (roles == null) roles = List.of();

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                AuthUserPrincipal principal = new AuthUserPrincipal(uid, username, null, authorities);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // token 失效：清空上下文，让后续 EntryPoint 输出 401，前端拿到后走刷新流程
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
