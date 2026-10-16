package com.linked.service;

import com.linked.dto.NewsArticleDto;

import java.util.List;

/** Noticias sobre tatuajes para la home del cliente web. */
public interface NewsService {

    /**
     * NewsAPI si hay {@code app.news.api-key}; si no hay clave o falla, los posts más votados de la
     * semana en Reddit r/tattoos. Lista vacía si también falla: la home no depende de esto.
     */
    List<NewsArticleDto> getLatestNews();
}
