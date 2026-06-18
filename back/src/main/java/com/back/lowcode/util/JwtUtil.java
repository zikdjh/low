package com.back.lowcode.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "lowcode-platform-secret-2026";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).build();

    public static String generateToken(String subject, Map<String, Object> claims, long expireSeconds) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000);
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create().withSubject(subject).withIssuedAt(now).withExpiresAt(expire);
        if (claims != null) {
            claims.forEach((key, value) -> {
                if (value instanceof Boolean) {
                    builder.withClaim(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    builder.withClaim(key, (Integer) value);
                } else if (value instanceof Long) {
                    builder.withClaim(key, (Long) value);
                } else if (value instanceof Double) {
                    builder.withClaim(key, (Double) value);
                } else {
                    builder.withClaim(key, value == null ? null : value.toString());
                }
            });
        }
        return builder.sign(ALGORITHM);
    }

    public static DecodedJWT verify(String token) {
        try {
            return VERIFIER.verify(token);
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("无效或已过期的访问令牌");
        }
    }
}
