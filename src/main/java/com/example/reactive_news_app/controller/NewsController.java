package com.example.reactive_news_app.controller;

import com.example.reactive_news_app.model.NewsArticle;
import com.example.reactive_news_app.serivce.NewsService;
import com.example.reactive_news_app.serivce.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final NewsService newsService;
    private final UserService userService;

    @GetMapping
    public Flux<NewsArticle> getAllNews() {
        log.info("GET /api/news - 모든 뉴스 조회");
        return newsService.getAllNews();
    }

    @GetMapping("/{id}")
    public Mono<NewsArticle> getNewsById(@PathVariable Long id) {
        log.info("GET /api/news/{} - 특정 뉴스 조회", id);
        return newsService.getNewsById(id);
    }

    @GetMapping("/category/{category}")
    public Flux<NewsArticle> getNewsByCategory(@PathVariable String category) {
        log.info("GET /api/news/category/{} - 카테고리별 뉴스 조회", category);
        return newsService.getNewsByCategory(category);
    }

    // Server-Sent Events
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NewsArticle> getNewsStream() {
        log.info("GET /api/news/stream - 실시간 뉴스 스트림 시작");

        return newsService.getNewsStream()
                .doOnSubscribe(subscription -> log.info("클라이언트가 뉴스 스트림 구독 시작"))
                .doOnCancel(() -> log.info("클라이언트가 뉴스 스트림 구독 취소"))
                .doOnTerminate(() -> log.info("뉴스 스트림 종료"));
    }

    @GetMapping("/personalized/{userId}")
    public Flux<NewsArticle> getPersonalizedNews(@PathVariable Long userId) {
        log.info("GET /api/news/personalized/{} - 개인화 뉴스 조회", userId);

        return userService.getUserPreferences(userId)
                .flatMapMany(newsService::getPersonalizedNews)
                .doOnNext(article -> log.debug("개인화 뉴스 반환: {}", article.getTitle()));
    }

    @GetMapping("/search")
    public Flux<NewsArticle> searchNews(@RequestParam String keyword) {
        log.info("GET /api/news/search?keyword={} - 뉴스 검색", keyword);
        return newsService.searchNews(keyword);
    }

    @GetMapping("/popular")
    public Flux<NewsArticle> getPopularNews(@RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/news/popular?limit={} - 인기 뉴스 조회", limit);
        return newsService.getPopularNews(limit);
    }

    @PostMapping
    public Mono<NewsArticle> createNews(@RequestBody NewsArticle newsArticle) {
        log.info("POST /api/news - 새 뉴스 생성: {}", newsArticle.getTitle());
        return newsService.createNews(newsArticle);
    }

    @GetMapping("/slow")
    public Flux<NewsArticle> getSlowNews() {
        log.info("GET /api/news/slow - 느린 뉴스 조회 (백프레셔 테스트)");

        return newsService.getAllNews()
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(article -> log.debug("느린 뉴스 반환: {}", article.getTitle()));
    }

    @GetMapping(value = "/stream-backpressure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NewsArticle> getNewsStreamBackpressure() {
        log.info("GET /api/news/stream-backpressure - 백프레셔 처리된 뉴스 스트림");

        return Flux.interval(Duration.ofMillis(100))
                .map(this::generateTestArticle)
                .onBackpressureBuffer(10)
                .doOnNext(article -> log.debug("백프레셔 스트림: {}", article.getTitle()))
                .doOnError(error -> log.error("백프레셔 오류: {}", error.getMessage()));
    }
}