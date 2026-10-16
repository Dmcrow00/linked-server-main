package com.linked.service.impl;

import com.linked.dto.NewsArticleDto;
import com.linked.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;

/** Implementación de {@link NewsService}. */
@Slf4j
@Service
public class NewsServiceImpl implements NewsService {

    static final String NEWS_API = "https://newsapi.org/v2/everything?q=tattoo&language=es&sortBy=publishedAt&pageSize=20&apiKey={clave}";
    static final String REDDIT = "https://www.reddit.com/r/tattoos/top.json?limit=20&t=week";

    private final RestClient http;
    private final String claveNewsApi;

    public NewsServiceImpl(RestClient.Builder http, @Value("${app.news.api-key:}") String claveNewsApi) {
        this.http = http.build();
        this.claveNewsApi = claveNewsApi;
    }

    @Override
    public List<NewsArticleDto> getLatestNews() {
        if (claveNewsApi == null || claveNewsApi.isBlank()) {
            return desdeReddit();
        }
        try {
            RespuestaNewsApi respuesta = http.get().uri(NEWS_API, claveNewsApi).retrieve().body(RespuestaNewsApi.class);
            if (respuesta == null || !"ok".equals(respuesta.status()) || respuesta.articles() == null) {
                return desdeReddit();
            }
            return respuesta.articles().stream()
                    .filter(articulo -> articulo.title() != null)
                    .map(articulo -> new NewsArticleDto(articulo.title(), articulo.url(), articulo.description(),
                            articulo.publishedAt(), articulo.source() != null ? articulo.source().name() : null))
                    .toList();
        } catch (RestClientException ex) {
            log.warn("NewsAPI no respondió, se usa Reddit: {}", ex.getMessage());
            return desdeReddit();
        }
    }

    private List<NewsArticleDto> desdeReddit() {
        try {
            RespuestaReddit respuesta = http.get().uri(REDDIT)
                    .header("User-Agent", "LinkedApp/1.0")
                    .retrieve()
                    .body(RespuestaReddit.class);
            if (respuesta == null || respuesta.data() == null || respuesta.data().children() == null) {
                return List.of();
            }
            return respuesta.data().children().stream()
                    .map(RespuestaReddit.Hijo::data)
                    .filter(Objects::nonNull)
                    .filter(post -> post.title() != null)
                    .map(post -> new NewsArticleDto(post.title(), "https://www.reddit.com" + post.permalink(),
                            post.selftext(), null, "Reddit r/tattoos"))
                    .toList();
        } catch (RestClientException ex) {
            log.warn("Reddit tampoco respondió: {}", ex.getMessage());
            return List.of();
        }
    }

    record RespuestaNewsApi(String status, List<Articulo> articles) {

        record Articulo(String title, String url, String description, String publishedAt, Fuente source) {
        }

        record Fuente(String name) {
        }
    }

    record RespuestaReddit(Datos data) {

        record Datos(List<Hijo> children) {
        }

        record Hijo(Post data) {
        }

        record Post(String title, String permalink, String selftext) {
        }
    }
}
