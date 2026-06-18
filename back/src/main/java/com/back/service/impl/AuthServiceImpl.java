package com.back.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.common.Constant;
import com.back.common.Result;
import com.back.config.security.AuthProperties;
import com.back.entity.dto.LoginRequest;
import com.back.entity.dto.RegisterRequest;
import com.back.entity.po.Role;
import com.back.entity.po.User;
import com.back.repository.RoleRepository;
import com.back.repository.UserRepository;
import com.back.service.AuthService;
import com.back.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 注册时默认绑定的角色编码 */
    private static final String DEFAULT_ROLE_CODE = "user";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthProperties props;

    @Override
    @Transactional
    public Result register(RegisterRequest req, HttpServletResponse response) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return Result.error("用户名已存在");
        }
        Role defaultRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException("默认角色 " + DEFAULT_ROLE_CODE + " 未初始化"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .roles(roles)
                .build();
        User saved = userRepository.save(user);
        log.info("注册新用户 id={} username={}", saved.getId(), saved.getUsername());

        return Result.success(buildLoginPayload(saved, response));
    }

    @Override
    public Result login(LoginRequest req, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        } catch (AuthenticationException e) {
            log.warn("登录异常 username={} : {}", req.getUsername(), e.getMessage());
            return Result.error("登录失败");
        }
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error("用户名或密码错误");
        }

        // 走到这里 username 一定有效；再查一次完整 User，下面要 id / nickname / roles
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalStateException("登录通过但用户记录缺失"));
        return Result.success(buildLoginPayload(user, response));
    }

    @Override
    public Result refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readRefreshCookie(request);
        if (!StringUtils.hasText(refreshToken)) {
            return Result.error("缺少 refresh token");
        }
        DecodedJWT jwt = jwtUtil.verify(refreshToken, Constant.REFRESH_TOKEN_TYPE);
        if (jwt == null) {
            clearRefreshCookie(response);
            return Result.error("refresh token 已失效");
        }
        Long uid = jwtUtil.getUid(jwt);
        User user = userRepository.findById(uid).orElse(null);
        if (user == null) {
            clearRefreshCookie(response);
            return Result.error("用户不存在");
        }

        // 仅刷新 access，refresh 保持不变（避免 sliding refresh 引起的越权链路过长）
        List<String> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
        String access = jwtUtil.issueAccessToken(user.getId(), user.getUsername(), roles);

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", access);
        payload.put("user", toUserView(user, roles));
        return Result.success(payload);
    }

    @Override
    public Result logout(HttpServletResponse response) {
        clearRefreshCookie(response);
        return Result.success();
    }

    /* ------------ 内部辅助 ------------ */

    private Map<String, Object> buildLoginPayload(User user, HttpServletResponse response) {
        List<String> roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
        String access = jwtUtil.issueAccessToken(user.getId(), user.getUsername(), roles);
        String refresh = jwtUtil.issueRefreshToken(user.getId(), user.getUsername());
        writeRefreshCookie(response, refresh);

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", access);
        payload.put("user", toUserView(user, roles));
        return payload;
    }

    private Map<String, Object> toUserView(User user, List<String> roles) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("nickname", user.getNickname());
        view.put("createdAt", user.getCreatedAt());
        view.put("roles", roles);
        return view;
    }

    private void writeRefreshCookie(HttpServletResponse response, String refresh) {
        AuthProperties.Cookie cfg = props.getCookie();
        Cookie cookie = new Cookie(cfg.getRefreshName(), refresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(cfg.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(props.getJwt().getRefreshExpireDays() * 24 * 60 * 60);
        // SameSite 在 Servlet Cookie 里没有原生 setter，依赖 setAttribute（Servlet 6 支持）
        cookie.setAttribute("SameSite", cfg.getSameSite());
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        AuthProperties.Cookie cfg = props.getCookie();
        Cookie cookie = new Cookie(cfg.getRefreshName(), "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cfg.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cfg.getSameSite());
        response.addCookie(cookie);
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (props.getCookie().getRefreshName().equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
