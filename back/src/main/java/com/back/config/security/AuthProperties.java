package com.back.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 鉴权相关配置（JWT + Cookie），对应 application.yaml 中的 auth.* 节。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Cookie cookie = new Cookie();

    @Data
    public static class Jwt {
        private String issuer = "lowcode-platform";
        private String secret;
        private int accessExpireMinutes = 45;
        private int refreshExpireDays = 7;
    }

    @Data
    public static class Cookie {
        private String refreshName = "refresh_token";
        private boolean secure = false;
        private String sameSite = "Lax";
    }
}
