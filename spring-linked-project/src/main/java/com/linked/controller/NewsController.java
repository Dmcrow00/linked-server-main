package com.linked.controller;

import com.dmc00.core.web.ApiResponse;
import com.linked.dto.NewsArticleDto;
import com.linked.security.Permisos;
import com.linked.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Feed público de noticias sobre tatuajes. */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "Noticias", description = "Feed de noticias sobre tatuajes")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Últimas noticias sobre tatuajes")
    public ResponseEntity<ApiResponse<List<NewsArticleDto>>> getNews() {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getLatestNews()));
    }
}
