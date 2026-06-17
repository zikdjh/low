package com.back.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.back.common.Constant;
import com.back.config.security.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * JWT 工具：access / refresh 双 token 的签发与校验。
 *
 * <ul>
 *   <li>access：Header {@code access}，载体为 {@code uid + username + roles}。</li>
 *   <li>refresh：HttpOnly Cookie，仅携带 {@code uid + username}，不放角色，避免 cookie 被读到完整信息。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";

    private final AuthProperties props;

    private Algorithm algorithm() {
        return Algorithm.HMAC256(props.getJwt().getSecret());
    }

    public String issueAccessToken(Long uid, String username, List<String> roles) {
        long now = System.currentTimeMillis();
        long expire = now + props.getJwt().getAccessExpireMinutes() * 60_000L;
        return JWT.create()
                .withIssuer(props.getJwt().getIssuer())
                .withSubject(username)
                .withClaim(CLAIM_UID, uid)
                .withClaim(CLAIM_ROLES, roles)
                .withClaim(CLAIM_TYPE, Constant.ACCESS_TOKEN_TYPE)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(expire))
                .sign(algorithm());
    }

    public String issueRefreshToken(Long uid, String username) {
        long now = System.currentTimeMillis();
        long expire = now + props.getJwt().getRefreshExpireDays() * 24L * 60 * 60 * 1000;
        return JWT.create()
                .withIssuer(props.getJwt().getIssuer())
                .withSubject(username)
                .withClaim(CLAIM_UID, uid)
                .withClaim(CLAIM_TYPE, Constant.REFRESH_TOKEN_TYPE)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(expire))
                .sign(algorithm());
    }

    /** 校验并返回解码后的 JWT；失败返回 null（调用方据此走 401 / 499）。 */
    public DecodedJWT verify(String token, String expectType) {
        try {
            JWTVerifier verifier = JWT.require(algorithm())
                    .withIssuer(props.getJwt().getIssuer())
                    .withClaim(CLAIM_TYPE, expectType)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return null;
        }
    }

    public Long getUid(DecodedJWT jwt) {
        return jwt.getClaim(CLAIM_UID).asLong();
    }

    public String getUsername(DecodedJWT jwt) {
        return jwt.getSubject();
    }

    public List<String> getRoles(DecodedJWT jwt) {
        return jwt.getClaim(CLAIM_ROLES).asList(String.class);
    }
}
