package com.back.config;

import com.back.interceptors.PathInterceptor;
import com.back.interceptors.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PathInterceptor pathInterceptor;
    private final TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pathInterceptor);
        registry.addInterceptor(tokenInterceptor);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源（适配 cnb 动态域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的 HTTP方法
                .allowedHeaders("*")  // 允许的 HTTP请求头
                .exposedHeaders("*")  // 暴露的 HTTP响应头
                .allowCredentials(true)  // 是否允许发送 Cookie
                .maxAge(3600);  // 预检请求的缓存时间（秒）
    }

}
