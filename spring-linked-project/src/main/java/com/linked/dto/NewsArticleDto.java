package com.linked.dto;

/**
 * Noticia del feed de tatuajes.
 *
 * @param source nombre del medio, o "Reddit r/tattoos" cuando se usa la fuente alternativa
 */
public record NewsArticleDto(
        String title,
        String url,
        String description,
        String publishedAt,
        String source
) {}
