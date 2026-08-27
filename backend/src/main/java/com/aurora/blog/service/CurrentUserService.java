package com.aurora.blog.service;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CurrentUserService {
    private final BlogMapper mapper;

    public CurrentUserService(BlogMapper mapper) { this.mapper = mapper; }

    public Map<String, Object> find(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        Map<String, Object> auth = mapper.findAuthUser(authentication.getName());
        if (auth == null) return null;
        return mapper.findSafeUser(number(auth.get("id")));
    }

    public long requireId(Authentication authentication) {
        Map<String, Object> user = find(authentication);
        if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录后再操作");
        return number(user.get("id"));
    }

    public static long number(Object value) {
        return ((Number) value).longValue();
    }
}
