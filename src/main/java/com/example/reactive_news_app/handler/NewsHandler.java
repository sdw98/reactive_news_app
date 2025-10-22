package com.example.reactive_news_app.handler;

import com.example.reactive_news_app.model.NewsArticle;
import com.example.reactive_news_app.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsHandler {
    private final NewsService newsService;

    public Mono<ServerResponse> getAllNews(ServerRequest request) {
        log.info("Handler: 모든 뉴스 조회 요청");

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(newsService.getAllNews(), NewsArticle.class)
                .doOnSuccess(response -> log.info("Handler: 뉴스 목록 응답 완료"));
    }

    public Mono<ServerResponse> getNewsById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        log.info("Hanlder: 뉴스 조회 요청 - ID: {}", id);

        return newsService.getNewsById(id)
                .flatMap(article -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(article))
                .switchIfEmpty(ServerResponse.notFound().build())
                .doOnSuccess(response -> log.info("Handler: 뉴스 조회 응답 완료 - ID: {}", id));
    }

    public Mono<ServerResponse> createNews(ServerRequest request) {
        log.info("Handler: 뉴스 생성 요청");

        return request.bodyToMono(NewsArticle.class)
                .flatMap(newsService::createNews)
                .flatMap(created -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(created)
                )
                .doOnSuccess(response -> log.info("Handler: 뉴스 생성 응답 완료"));
    }

    public Mono<ServerResponse> getNewsStream(ServerRequest request) {
        log.info("Handler: 실시간 뉴스 스트림 요청");

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(newsService.getNewsStream(), NewsArticle.class)
                .doOnSuccess(response -> log.info("Handler: 뉴스 스트림 응답 시작"));
    }

    public Mono<ServerResponse> getNewsByCategory(ServerRequest request) {
        String category = request.pathVariable("category");
        log.info("Handler: 카테고리별 뉴스 조회 요청 - Category: {}", category);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(newsService.getNewsByCategory(category), NewsArticle.class)
                .doOnSuccess(response -> log.info("Handler: 카테고리별 뉴스 응답 완료 - Category: {}", category));
    }
}