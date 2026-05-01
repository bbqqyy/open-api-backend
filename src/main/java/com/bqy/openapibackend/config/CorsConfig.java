package com.bqy.openapibackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${avatar.upload.dir:./uploads/avatars}")
    private String avatarUploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将本地头像目录映射为静态资源，通过 /uploads/avatars/** 可访问
        // 使用 NIO Paths 获取绝对路径，避免 Tomcat 工作目录问题
        String absolutePath = Paths.get(avatarUploadDir).toAbsolutePath().normalize().toString();
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
