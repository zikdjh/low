package com.back.lowcode.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.lowcode.entity.UserAccount;
import com.back.lowcode.repository.UserAccountRepository;
import com.back.lowcode.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private static final ConcurrentHashMap<String, String> PHONE_CODE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> PHONE_CODE_EXPIRE = new ConcurrentHashMap<>();

    public String sendPhoneCode(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 6) {
            throw new IllegalArgumentException("手机号格式不合法");
        }
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        PHONE_CODE_CACHE.put(phone, code);
        PHONE_CODE_EXPIRE.put(phone, new Date().getTime() + 5 * 60 * 1000);
        log.info("[Mock SMS] phone={} code={}", phone, code);
        return code;
    }

    @Transactional
    public String login(String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            throw new IllegalArgumentException("手机号或验证码不能为空");
        }
        String expected = PHONE_CODE_CACHE.get(phone);
        Long expireAt = PHONE_CODE_EXPIRE.get(phone);
        if (expected == null || !expected.equals(code) || expireAt == null || expireAt < new Date().getTime()) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        UserAccount user = userAccountRepository.findByPhone(phone)
                .orElseGet(() -> userAccountRepository.save(UserAccount.builder()
                        .phone(phone)
                        .username("用户" + phone.substring(phone.length() - 4))
                        .build()));

        Map<String, Object> claims = Map.of("phone", user.getPhone(), "userId", user.getId());
        return JwtUtil.generateToken(user.getPhone(), claims, 45 * 60);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("刷新令牌缺失");
        }
        DecodedJWT decoded = JwtUtil.verify(refreshToken);
        String phone = decoded.getClaim("phone").asString();
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("刷新令牌无效");
        }
        UserAccount user = userAccountRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + phone));
        return JwtUtil.generateToken(user.getPhone(), Map.of("phone", user.getPhone(), "userId", user.getId()), 45 * 60);
    }

    public UserAccount getUserFromAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("访问令牌缺失");
        }
        DecodedJWT decoded = JwtUtil.verify(token);
        String phone = decoded.getClaim("phone").asString();
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("访问令牌无效");
        }
        return userAccountRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + phone));
    }

    public String generateRefreshToken(UserAccount user) {
        return JwtUtil.generateToken(user.getPhone(), Map.of("phone", user.getPhone(), "userId", user.getId()), 7 * 24 * 60 * 60);
    }
}
