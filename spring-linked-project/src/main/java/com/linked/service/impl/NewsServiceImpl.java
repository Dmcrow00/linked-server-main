package com.linked.service.impl;

import com.linked.dto.NewsArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Noticias sobre tatuajes para la home del cliente web.
 * <p>
 * Usa NewsAPI cuando hay {@code app.news.api-key} configurada. Si no hay clave o la llamada
 * falla, recurre a los posts más votados de la semana en Reddit r/tattoos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.news.api-key:}")
    private String newsApiKey;

    @SuppressWarnings("unchecked")
    public List<NewsArticleDto> getLatestNews() {
        if (newsApiKey == null || newsApiKey.isBlank()) {
            return getFallbackNews();
        }

        try {
            Map<?, ?> response = webClientBuilder.build()
                    .get()
                    .uri("https://newsapi.org/v2/everything" +
                         "?q=tattoo&language=es&sortBy=publishedAt&pageSize=20" +
                         "&apiKey=" + newsApiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !"ok".equals(response.get("status"))) {
                return getFallbackNews();
            }

            List<Map<?, ?>> articles = (List<Map<?, ?>>) response.get("articles");
            if (articles == null) return List.of();

            return articles.stream()
                    .map(a -> new NewsArticleDto(
                            (String) a.get("title"),
                            (String) a.get("url"),
                            (String) a.get("description"),
                            (String) a.get("publishedAt"),
                            a.get("source") instanceof Map<?, ?> src ? (String) src.get("name") : null
                    ))
                    .filter(dto -> dto.title() != null)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch news: {}", e.getMessage());
            return getFallbackNews();
        }
    }

    /** Posts destacados de la semana en Reddit r/tattoos; devuelve lista vacía si también falla. */
    @SuppressWarnings("unchecked")
    private List<NewsArticleDto> getFallbackNews() {
        try {
            Map<?, ?> response = webClientBuilder.build()
                    .get()
                    .uri("https://www.reddit.com/r/tattoos/top.json?limit=20&t=week")
                    .header("User-Agent", "LinkedApp/1.0")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return List.of();

            Map<?, ?> data = (Map<?, ?>) response.get("data");
            if (data == null) return List.of();

            List<Map<?, ?>> children = (List<Map<?, ?>>) data.get("children");
            if (children == null) return List.of();

            return children.stream()
                    .map(child -> {
                        Map<?, ?> post = (Map<?, ?>) child.get("data");
                        return new NewsArticleDto(
                                (String) post.get("title"),
                                "https://www.reddit.com" + post.get("permalink"),
                                (String) post.get("selftext"),
                                null,
                                "Reddit r/tattoos"
                        );
                    })
                    .filter(dto -> dto.title() != null)
                    .toList();

        } catch (Exception e) {
            log.warn("Reddit fallback also failed: {}", e.getMessage());
            return List.of();
        }
    }
}
