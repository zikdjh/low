package com.back.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自带 uid 的 UserDetails，方便 Filter 直接拿到主键放进 SecurityContext。
 */
@Getter
public class AuthUserPrincipal extends User {

    private final Long uid;

    public AuthUserPrincipal(Long uid,
                             String username,
                             String password,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password == null ? "" : password, authorities);
        this.uid = uid;
    }
}
