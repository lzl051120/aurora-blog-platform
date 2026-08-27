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
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final BlogMapper mapper;
    private final BlogService service;
    private final CurrentUserService currentUser;

    public AdminController(BlogMapper mapper, BlogService service, CurrentUserService currentUser) {
        this.mapper = mapper;
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "posts", mapper.countPosts(),
                "published", mapper.countPublished(),
                "users", mapper.countUsers(),
                "pendingComments", mapper.countPendingComments());
    }

    @GetMapping("/posts")
    public List<Map<String, Object>> posts(@RequestParam(required = false) String status) {
        return mapper.listAdminPosts(status);
    }

    @GetMapping("/posts/{id}")
    public Map<String, Object> post(@PathVariable long id) {
        Map<String, Object> post = mapper.findPostForAdmin(id);
        if (post == null) throw new ApiException(HttpStatus.NOT_FOUND, "文章不存在");
        return post;
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createPost(@Valid @RequestBody PostRequest body, Authentication authentication) {
        return service.createPost(currentUser.requireId(authentication), body.categoryId(), body.title(), body.slug(),
                body.summary(), body.contentHtml(), body.coverUrl(), body.status());
    }

    @PutMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePost(@PathVariable long id, @Valid @RequestBody PostRequest body, Authentication authentication) {
        service.updatePost(id, currentUser.requireId(authentication), body.categoryId(), body.title(), body.slug(),
                body.summary(), body.contentHtml(), body.coverUrl(), body.status());
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable long id, Authentication authentication) {
        service.deletePost(id, currentUser.requireId(authentication));
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@Valid @RequestBody CategoryRequest body, Authentication authentication) {
        mapper.insertCategory(body.name().trim(), body.slug().trim(), value(body.description()), body.sortOrder());
        mapper.insertAudit(currentUser.requireId(authentication), "CREATE_CATEGORY", "CATEGORY", body.slug(), body.name());
    }

    @PutMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategory(@PathVariable long id, @Valid @RequestBody CategoryRequest body, Authentication authentication) {
        if (mapper.updateCategory(id, body.name().trim(), body.slug().trim(), value(body.description()), body.sortOrder()) == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        mapper.insertAudit(currentUser.requireId(authentication), "UPDATE_CATEGORY", "CATEGORY", String.valueOf(id), body.name());
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long id, Authentication authentication) {
        if (mapper.deleteCategory(id) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "分类不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "DELETE_CATEGORY", "CATEGORY", String.valueOf(id), "删除分类");
    }

    @GetMapping("/comments")
    public List<Map<String, Object>> comments() { return mapper.listAllComments(); }

    @PatchMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moderateComment(@PathVariable long id, @Valid @RequestBody StatusRequest body, Authentication authentication) {
        validateModeration(body.status());
        if (mapper.moderateComment(id, body.status()) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "评论不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "MODERATE_COMMENT", "COMMENT", String.valueOf(id), body.status());
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long id, Authentication authentication) {
        if (mapper.deleteComment(id) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "评论不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "DELETE_COMMENT", "COMMENT", String.valueOf(id), "删除评论");
    }

    @GetMapping("/guestbook")
    public List<Map<String, Object>> guestbook() { return mapper.listAllGuestbook(); }

    @PatchMapping("/guestbook/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moderateGuestbook(@PathVariable long id, @Valid @RequestBody StatusRequest body, Authentication authentication) {
        validateModeration(body.status());
        if (mapper.moderateGuestbook(id, body.status()) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "留言不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "MODERATE_GUESTBOOK", "GUESTBOOK", String.valueOf(id), body.status());
    }

    @DeleteMapping("/guestbook/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuestbook(@PathVariable long id, Authentication authentication) {
        if (mapper.deleteGuestbook(id) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "留言不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "DELETE_GUESTBOOK", "GUESTBOOK", String.valueOf(id), "删除留言");
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() { return mapper.listUsers(); }

    @PatchMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void userStatus(@PathVariable long id, @Valid @RequestBody UserStatusRequest body, Authentication authentication) {
        if (!Set.of("ACTIVE", "DISABLED").contains(body.status())) throw new ApiException(HttpStatus.BAD_REQUEST, "用户状态无效");
        if (mapper.updateUserStatus(id, body.status()) == 0) throw new ApiException(HttpStatus.BAD_REQUEST, "不能修改管理员或用户不存在");
        mapper.insertAudit(currentUser.requireId(authentication), "UPDATE_USER_STATUS", "USER", String.valueOf(id), body.status());
    }

    private void validateModeration(String status) {
        if (!Set.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "审核状态无效");
        }
    }

    private String value(String value) { return value == null ? "" : value.trim(); }

    public record PostRequest(Long categoryId,
                              @NotBlank @Size(max = 160) String title,
                              @Size(max = 180) String slug,
                              @NotBlank @Size(max = 360) String summary,
                              @NotBlank String contentHtml,
                              @Size(max = 500) String coverUrl,
                              @NotBlank String status) {}

    public record CategoryRequest(@NotBlank @Size(max = 48) String name,
                                  @NotBlank @Size(max = 64) String slug,
                                  @Size(max = 200) String description,
                                  int sortOrder) {}

    public record StatusRequest(@NotBlank String status) {}
    public record UserStatusRequest(@NotBlank String status) {}
}
