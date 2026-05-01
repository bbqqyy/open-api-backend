package com.bqy.openapibackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件访问控制器
 * 提供头像等静态文件的访问，绕开 Spring 静态资源映射的路径解析问题
 */
@Tag(name = "文件访问", description = "访问本地存储的静态文件（头像等）")
@RestController
@RequestMapping("/uploads")
@Slf4j
public class FileController {

    @Value("${avatar.upload.dir:/Users/bqy0922/IdeaProjects/open-api-backend/uploads/avatars}")
    private String avatarUploadDir;

    @Operation(summary = "访问头像文件", description = "根据文件名返回头像图片")
    @GetMapping("/avatars/{filename}")
    public void getAvatar(@PathVariable String filename, HttpServletResponse response) throws IOException {
        // 防止路径穿越攻击：文件名不能包含路径分隔符
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "非法文件名");
            return;
        }

        Path filePath = Paths.get(avatarUploadDir).toAbsolutePath().normalize().resolve(filename);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.warn("头像文件不存在或不可读: {}", filePath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 自动识别 MIME 类型
        String contentType = URLConnection.guessContentTypeFromName(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setContentLengthLong(Files.size(filePath));
        // 允许浏览器缓存 7 天
        response.setHeader("Cache-Control", "public, max-age=604800");

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
        } catch (IOException e) {
            log.error("头像文件输出失败: {}", filePath, e);
        }
    }
}

