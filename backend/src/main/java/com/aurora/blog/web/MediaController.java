package com.aurora.blog.web;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/media")
public class MediaController {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final BlogMapper mapper;
    private final CurrentUserService currentUser;
    private final Path uploadDir;

    public MediaController(BlogMapper mapper, CurrentUserService currentUser,
                           @Value("${aurora.upload-dir}") String uploadDir) {
        this.mapper = mapper;
        this.currentUser = currentUser;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping
    public List<Map<String, Object>> list() { return mapper.listMedia(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024L) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "图片不能为空且不得超过5MB");
        }
        String contentType = file.getContentType();
        String extension = EXTENSIONS.get(contentType);
        if (extension == null || !matchesMagic(file, contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持真实的 JPEG、PNG 或 WebP 图片");
        }
        Files.createDirectories(uploadDir);
        String storedName = UUID.randomUUID() + extension;
        Path destination = uploadDir.resolve(storedName).normalize();
        if (!destination.startsWith(uploadDir)) throw new ApiException(HttpStatus.BAD_REQUEST, "文件路径无效");
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        String publicUrl = "/media/" + storedName;
        long userId = currentUser.requireId(authentication);
        mapper.insertMedia(userId, Optional.ofNullable(file.getOriginalFilename()).orElse("image"), storedName,
                contentType, file.getSize(), publicUrl);
        mapper.insertAudit(userId, "UPLOAD_MEDIA", "MEDIA", storedName, String.valueOf(file.getOriginalFilename()));
        return Map.of("url", publicUrl, "name", storedName, "size", file.getSize());
    }

    private boolean matchesMagic(MultipartFile file, String contentType) throws IOException {
        byte[] bytes;
        try (InputStream input = file.getInputStream()) { bytes = input.readNBytes(12); }
        if ("image/png".equals(contentType)) {
            return bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
        }
        if ("image/jpeg".equals(contentType)) {
            return bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF;
        }
        return bytes.length >= 12 && new String(bytes, 0, 4).equals("RIFF") && new String(bytes, 8, 4).equals("WEBP");
    }
}
