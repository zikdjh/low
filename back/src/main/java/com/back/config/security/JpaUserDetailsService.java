package com.back.config.security;

import com.back.entity.po.User;
import com.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 把 JPA 中的 {@link User} 适配成 Spring Security 的 {@link UserDetails}。
 * 角色权限以 {@code ROLE_<code>} 形式输出，便于后续 {@code hasRole("admin")} 这类断言。
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        if ("frozen".equals(user.getStatus())) {
            throw new UsernameNotFoundException("账号已被冻结");
        }
        return toPrincipal(user);
    }

    public static AuthUserPrincipal toPrincipal(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getCode()))
                .collect(Collectors.toSet());
        return new AuthUserPrincipal(user.getId(), user.getUsername(), user.getPassword(), authorities);
    }
}
