package com.aurora.blog.service;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.model.PostDraft;
import com.aurora.blog.web.ApiException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Duration;
import java.util.*;

@Service
public class BlogService {
    private final BlogMapper mapper;
    private final StringRedisTemplate redis;
    private final RateLimitService rateLimit;

    public BlogService(BlogMapper mapper, StringRedisTemplate redis, RateLimitService rateLimit) {
        this.mapper = mapper;
        this.redis = redis;
        this.rateLimit = rateLimit;
    }

    public Map<String, Object> home() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("featured", mapper.listPublishedPosts(null, null, "hot", 3, 0));
        body.put("latest", mapper.listPublishedPosts(null, null, "latest", 6, 0));
        body.put("categories", mapper.listCategories());
        body.put("stats", Map.of("posts", mapper.countPublished(), "users", mapper.countUsers()));
        return body;
    }

    public Map<String, Object> listPosts(String search, String category, String sort, int page, int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 24);
        String safeSort = "hot".equals(sort) ? "hot" : "latest";
        List<Map<String, Object>> items = mapper.listPublishedPosts(trim(search), trim(category), safeSort, size, (page - 1) * size);
        long total = mapper.countPublishedPosts(trim(search), trim(category));
        return Map.of("items", items, "page", page, "size", size, "total", total,
                "pages", Math.max(1, (total + size - 1) / size));
    }

    @Transactional
    public Map<String, Object> postDetail(String slug) {
        Map<String, Object> post = mapper.findPublishedPostBySlug(slug);
        if (post == null) throw new ApiException(HttpStatus.NOT_FOUND, "文章不存在或尚未发布");
        long id = CurrentUserService.number(post.get("id"));
        mapper.incrementPostView(id);
        try {
            redis.opsForZSet().incrementScore("aurora:hot:posts", String.valueOf(id), 1);
            redis.expire("aurora:hot:posts", Duration.ofDays(30));
        } catch (RuntimeException ignored) {
            // 浏览计数以 MySQL 为事实源，Redis 故障不阻断文章阅读。
        }
        post.put("viewCount", mapper.findPostViewCount(id));
        post.put("comments", mapper.listApprovedComments(id));
        return post;
    }

    public void addComment(long postId, long userId, Long parentId, String content) {
        rateLimit.check("comment:" + userId, 8, Duration.ofMinutes(10));
        mapper.insertComment(postId, userId, parentId, cleanPlainText(content, 1000));
    }

    public void addGuestbook(long userId, String content) {
        rateLimit.check("guestbook:" + userId, 5, Duration.ofMinutes(30));
        mapper.insertGuestbook(userId, cleanPlainText(content, 1000));
    }

    @Transactional
    public Map<String, Object> createPost(long authorId, Long categoryId, String title, String requestedSlug,
                                          String summary, String contentHtml, String coverUrl, String status) {
        PostDraft draft = normalizePost(authorId, categoryId, title, requestedSlug, summary, contentHtml, coverUrl, status, null);
        mapper.insertPost(draft);
        mapper.insertAudit(authorId, "CREATE_POST", "POST", draft.slug(), draft.title());
        return mapper.findPublishedPostBySlug(draft.slug()) != null
                ? mapper.findPublishedPostBySlug(draft.slug())
                : mapper.listAdminPosts(null).stream().filter(p -> draft.slug().equals(p.get("slug"))).findFirst().orElse(Map.of());
    }

    @Transactional
    public void updatePost(long id, long actorId, Long categoryId, String title, String requestedSlug,
                           String summary, String contentHtml, String coverUrl, String status) {
        Map<String, Object> existing = mapper.findPostForAdmin(id);
        if (existing == null) throw new ApiException(HttpStatus.NOT_FOUND, "文章不存在");
        PostDraft draft = normalizePost(actorId, categoryId, title, requestedSlug, summary, contentHtml, coverUrl, status, id);
        mapper.updatePost(id, draft);
        mapper.insertAudit(actorId, "UPDATE_POST", "POST", String.valueOf(id), draft.title());
    }

    @Transactional
    public void deletePost(long id, long actorId) {
        if (mapper.deletePost(id) == 0) throw new ApiException(HttpStatus.NOT_FOUND, "文章不存在");
        mapper.insertAudit(actorId, "DELETE_POST", "POST", String.valueOf(id), "删除文章");
    }

    public String cleanPlainText(String input, int max) {
        String value = input == null ? "" : Jsoup.parse(input).text().trim();
        if (value.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "内容不能为空");
        if (value.length() > max) throw new ApiException(HttpStatus.BAD_REQUEST, "内容最多" + max + "个字符");
        return value;
    }

    private PostDraft normalizePost(long authorId, Long categoryId, String title, String requestedSlug,
                                    String summary, String contentHtml, String coverUrl, String status, Long currentId) {
        title = cleanPlainText(title, 160);
        summary = cleanPlainText(summary, 360);
        if (!Set.of("DRAFT", "PUBLISHED").contains(status)) throw new ApiException(HttpStatus.BAD_REQUEST, "文章状态无效");
        String slug = slugify(requestedSlug == null || requestedSlug.isBlank() ? title : requestedSlug);
        if (slug.isBlank()) slug = "post-" + Long.toString(System.currentTimeMillis(), 36);
        if (mapper.countPostSlug(slug, currentId) > 0) slug += "-" + UUID.randomUUID().toString().substring(0, 6);
        Safelist allowlist = Safelist.relaxed()
                .addTags("figure", "figcaption", "mark", "s")
                .addAttributes("img", "loading", "width", "height")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https");
        String cleanedHtml = Jsoup.clean(contentHtml == null ? "" : contentHtml, allowlist);
        if (Jsoup.parse(cleanedHtml).text().isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "文章正文不能为空");
        String safeCover = coverUrl == null ? null : coverUrl.trim();
        return new PostDraft(authorId, categoryId, title, slug, summary, cleanedHtml, safeCover, status);
    }

    private String slugify(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim().replaceAll("[\\s-]+", "-");
    }

    private String trim(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
