package com.aurora.blog.service;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.model.NewUser;
import com.aurora.blog.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private final BlogMapper mapper;
    private final PasswordEncoder encoder;

    public AuthService(BlogMapper mapper, PasswordEncoder encoder) {
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Transactional
    public Map<String, Object> register(String username, String email, String password, String displayName) {
        username = username == null ? "" : username.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        displayName = displayName == null ? "" : displayName.trim();
        if (!USERNAME.matcher(username).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名需为3-32位字母、数字或下划线");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请输入有效邮箱地址");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码长度需为8-72位");
        }
        if (displayName.isBlank() || displayName.length() > 48) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "昵称不能为空且最多48个字符");
        }
        if (mapper.countDuplicateUser(username, email) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名或邮箱已被使用");
        }
        NewUser user = new NewUser(username, email, encoder.encode(password), displayName, "USER");
        mapper.insertUser(user);
        return mapper.findSafeUser(user.getId());
    }

    public void changePassword(long userId, String currentPassword, String newPassword) {
        Map<String, Object> safe = mapper.findSafeUser(userId);
        Map<String, Object> auth = mapper.findAuthUser(String.valueOf(safe.get("username")));
        if (!encoder.matches(currentPassword, String.valueOf(auth.get("passwordHash")))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密码长度需为8-72位");
        }
        mapper.updatePassword(userId, encoder.encode(newPassword));
    }
}
