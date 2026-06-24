package com.back.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 6 过滤链。
 *
 * <ul>
 *   <li>STATELESS：不开启 Session，认证完全由 JWT 承担。</li>
 *   <li>禁用 CSRF / formLogin / httpBasic / logout：避免 starter 默认行为污染 RESTful API。</li>
 *   <li>放行：{@code /auth/register}、{@code /auth/login}、{@code /auth/refresh}、OPTIONS 预检。</li>
 *   <li>{@link JwtAuthenticationFilter} 放在用户名密码过滤器之前。</li>
 *   <li>401/403 统一由 {@link JsonAuthFailureHandlers} 输出 {@code Result} JSON。</li>
 *   <li>CORS 由 Security 接管（与原 WebConfig 等价配置）。</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JpaUserDetailsService userDetailsService;
    private final JsonAuthFailureHandlers handlers;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // H2 控制台等场景需要，关闭 X-Frame-Options 以放行 iframe；同源仍可用
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(handlers.entryPoint())
                        .accessDeniedHandler(handlers.accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        // 预检请求一律放行，避免被 EntryPoint 401 拦截
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers("/auth/logout").permitAll()
                        // 管理员端：仅 admin / root 角色可访问
                        .requestMatchers("/admin/**").hasAnyRole("admin", "root")
                        // 低代码平台管理接口（实体/页面设计器等） — 临时放行
                        .requestMatchers("/lowcode/entity/**").permitAll()
                        .requestMatchers("/lowcode/data/**").permitAll()
                        .requestMatchers("/lowcode/page/**").permitAll()
                        .requestMatchers("/lowcode/component/**").permitAll()
                        .requestMatchers("/lowcode/dict/**").permitAll()
                        .requestMatchers("/lowcode/app/**").permitAll()
                        .requestMatchers("/lowcode/release/**").permitAll()
                        // 请假管理接口 — 需要认证
                        .requestMatchers("/lowcode/leave/admin/**").hasAnyRole("admin")
                        .requestMatchers("/lowcode/leave/**").authenticated()
                        // 通知管理接口 — 公开获取已发布通知，其他需要ADMIN角色
                        .requestMatchers("/api/notifications/published").permitAll()
                        .requestMatchers("/api/notifications/**").hasAnyRole("admin", "root")
                        // error 默认派发路径
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        // Security 6.4+ 推荐使用构造器形式，避免 setUserDetailsService/setPasswordEncoder 的 deprecation 告警
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * 等价原 WebConfig.addCorsMappings：
     * 允许所有来源 + 凭证、所有方法、所有头。
     * 由 Security 链管理后，OPTIONS 预检在过滤链最早期就放行（见 authorizeHttpRequests）。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
