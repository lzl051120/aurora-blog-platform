package com.aurora.blog.service;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.model.PostDraft;
import com.aurora.blog.web.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlogServiceTest {
    private final BlogMapper mapper = mock(BlogMapper.class);
    private final BlogService service = new BlogService(mapper, mock(StringRedisTemplate.class), mock(RateLimitService.class));

    @Test
    void plainTextStripsMarkupAndRejectsBlankInput() {
        assertEquals("你好 世界", service.cleanPlainText("<b>你好</b> <script>alert(1)</script>世界", 20));
        assertThrows(ApiException.class, () -> service.cleanPlainText("<img src=x>", 20));
    }

    @Test
    void createPostRemovesScriptAndUnsafeAttributes() {
        when(mapper.findPublishedPostBySlug(anyString())).thenReturn(Map.of("id", 11L, "slug", "safe-post"));

        service.createPost(1L, 2L, "Safe Post", "safe-post", "摘要",
                "<h2 onclick=alert(1)>标题</h2><script>alert(1)</script><p>正文</p>", null, "PUBLISHED");

        var captor = org.mockito.ArgumentCaptor.forClass(PostDraft.class);
        verify(mapper).insertPost(captor.capture());
        assertFalse(captor.getValue().contentHtml().contains("script"));
        assertFalse(captor.getValue().contentHtml().contains("onclick"));
        assertTrue(captor.getValue().contentHtml().contains("正文"));
    }
}
