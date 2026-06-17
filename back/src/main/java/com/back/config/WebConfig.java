package com.back.config;

import com.back.interceptors.PathInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：仅注册业务拦截器。
 * CORS 已交由 {@code SecurityConfig.corsConfigurationSource()} 统一管理，
 * 这里若再写 addCorsMappings 会导致两套 CORS 处理器并存、预检冲突。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PathInterceptor pathInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pathInterceptor);
    }
}
