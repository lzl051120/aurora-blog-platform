package com.aurora.blog.web;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.service.AuthService;
import com.aurora.blog.service.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final BlogMapper mapper;
    private final CurrentUserService currentUser;
    private final AuthService authService;

    public ProfileController(BlogMapper mapper, CurrentUserService currentUser, AuthService authService) {
        this.mapper = mapper;
        this.currentUser = currentUser;
        this.authService = authService;
    }

    @PutMapping
    public Map<String, Object> update(@Valid @RequestBody ProfileRequest body, Authentication authentication) {
        long id = currentUser.requireId(authentication);
        mapper.updateProfile(id, body.displayName().trim(), body.bio() == null ? "" : body.bio().trim(),
                body.avatarUrl() == null ? null : body.avatarUrl().trim());
        return mapper.findSafeUser(id);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@Valid @RequestBody PasswordRequest body, Authentication authentication) {
        authService.changePassword(currentUser.requireId(authentication), body.currentPassword(), body.newPassword());
    }

    public record ProfileRequest(@NotBlank @Size(max = 48) String displayName,
                                 @Size(max = 240) String bio,
                                 @Size(max = 500) String avatarUrl) {}

    public record PasswordRequest(@NotBlank String currentPassword,
                                  @NotBlank @Size(min = 8, max = 72) String newPassword) {}
}
