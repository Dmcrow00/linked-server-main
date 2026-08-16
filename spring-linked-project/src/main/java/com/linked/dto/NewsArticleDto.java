package com.linked.dto;

public record NewsArticleDto(
        String title,
        String url,
        String description,
        String publishedAt,
        String source
) {}
