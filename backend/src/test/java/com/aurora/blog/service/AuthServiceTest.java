package com.aurora.blog.service;

import com.aurora.blog.mapper.BlogMapper;
import com.aurora.blog.model.NewUser;
import com.aurora.blog.web.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final BlogMapper mapper = mock(BlogMapper.class);
    private final AuthService service = new AuthService(mapper, new BCryptPasswordEncoder());

    @Test
    void registerNormalizesEmailAndHashesPassword() {
        when(mapper.findSafeUser(anyLong())).thenReturn(Map.of("username", "reader_01"));
        doAnswer(invocation -> {
            NewUser user = invocation.getArgument(0);
            user.setId(7L);
            return 1;
        }).when(mapper).insertUser(any(NewUser.class));

        service.register("reader_01", " Reader@Example.COM ", "ReaderPass123!", " 微光读者 ");

        var captor = org.mockito.ArgumentCaptor.forClass(NewUser.class);
        verify(mapper).insertUser(captor.capture());
        assertEquals("reader@example.com", captor.getValue().getEmail());
        assertEquals("微光读者", captor.getValue().getDisplayName());
        assertNotEquals("ReaderPass123!", captor.getValue().getPasswordHash());
    }

    @Test
    void registerRejectsInvalidUsernameAndWeakPassword() {
        assertThrows(ApiException.class, () -> service.register("中", "a@example.com", "12345678", "读者"));
        assertThrows(ApiException.class, () -> service.register("reader", "a@example.com", "123", "读者"));
        verifyNoInteractions(mapper);
    }
}
