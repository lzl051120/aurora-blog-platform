package com.aurora.blog.model;

public record PostDraft(
        Long authorId,
        Long categoryId,
        String title,
        String slug,
        String summary,
        String contentHtml,
        String coverUrl,
        String status
) {}
