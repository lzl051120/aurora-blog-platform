package com.aurora.blog.web;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.service.BlogService;
import com.aurora.blog.service.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PublicController {
    private final BlogMapper mapper;
    private final BlogService service;
    private final CurrentUserService currentUser;

    public PublicController(BlogMapper mapper, BlogService service, CurrentUserService currentUser) {
        this.mapper = mapper;
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/home")
    public Map<String, Object> home() { return service.home(); }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() { return mapper.listCategories(); }

    @GetMapping("/posts")
    public Map<String, Object> posts(@RequestParam(required = false) String search,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(defaultValue = "latest") String sort,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "9") int size) {
        return service.listPosts(search, category, sort, page, size);
    }

    @GetMapping("/posts/{slug}")
    public Map<String, Object> post(@PathVariable String slug) { return service.postDetail(slug); }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> comment(@PathVariable long postId, @Valid @RequestBody ContentRequest body,
                                       Authentication authentication) {
        service.addComment(postId, currentUser.requireId(authentication), body.parentId(), body.content());
        return Map.of("message", "评论已提交，审核通过后展示");
    }

    @GetMapping("/guestbook")
    public List<Map<String, Object>> guestbook() { return mapper.listApprovedGuestbook(); }

    @PostMapping("/guestbook")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> guestbook(@Valid @RequestBody ContentRequest body, Authentication authentication) {
        service.addGuestbook(currentUser.requireId(authentication), body.content());
        return Map.of("message", "留言已提交，审核通过后展示");
    }

    public record ContentRequest(@NotBlank @Size(max = 1000) String content, Long parentId) {}
}
