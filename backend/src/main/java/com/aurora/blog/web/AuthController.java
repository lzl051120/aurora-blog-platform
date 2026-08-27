package com.aurora.blog.web;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.service.AuthService;
import com.aurora.blog.service.CurrentUserService;
import com.aurora.blog.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final AuthService authService;
    private final CurrentUserService currentUser;
    private final BlogMapper mapper;
    private final RateLimitService rateLimit;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository contextRepository,
                          AuthService authService, CurrentUserService currentUser, BlogMapper mapper,
                          RateLimitService rateLimit) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.authService = authService;
        this.currentUser = currentUser;
        this.mapper = mapper;
        this.rateLimit = rateLimit;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> user = currentUser.find(authentication);
        return Map.of("authenticated", user != null, "user", user == null ? Map.of() : user);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest body) {
        return authService.register(body.username(), body.email(), body.password(), body.displayName());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body,
                                     HttpServletRequest request, HttpServletResponse response) {
        rateLimit.check("login:" + request.getRemoteAddr() + ":" + body.login(), 10, Duration.ofMinutes(10));
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(body.login(), body.password()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            contextRepository.saveContext(context, request, response);
            Map<String, Object> authRow = mapper.findAuthUser(authentication.getName());
            return mapper.findSafeUser(((Number) authRow.get("id")).longValue());
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "账号或密码不正确");
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @NotBlank @Email @Size(max = 128) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 48) String displayName) {}

    public record LoginRequest(@NotBlank String login, @NotBlank String password) {}
}
