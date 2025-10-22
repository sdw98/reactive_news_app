package com.example.reactive_news_app.service;

import com.example.reactive_news_app.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class NewsService {

    private final Map<Long, NewsArticle> newsRepository = new HashMap<>();
    private final List<String> categories = Arrays.asList(
            "TECH", "SPORTS", "POLITICS", "ENTERTAINMENT", "SCIENCE"
    );
    private final List<String> authors = Arrays.asList("김기자", "이기자", "박기자", "최기자", "정기자");

    public NewsService() {
        initializeData();
    }

    public Flux<NewsArticle> getAllNews() {
        log.info("모든 뉴스 조회 요청");

        return Flux.fromIterable(newsRepository.values())
                .sort(Comparator.comparing(NewsArticle::getPublishedAt))
                .doOnNext(article -> log.debug("뉴스 반환: {}", article.getTitle()))
                .doOnComplete(() -> log.info("모든 뉴스 조회 완료"));
    }

    public Mono<NewsArticle> getNewsById(Long id) {
        log.info("뉴스 조회 요청: ID={}", id);

        return Mono.fromSupplier(() -> newsRepository.get(id))
                .switchIfEmpty(Mono.error(new RuntimeException("뉴스를 찾을 수 없습니다: " + id)))
                .doOnNext(article -> {
                    article.setViewCount(article.getViewCount() + 1);
                    log.info("뉴스 조회 완료: {} (조회수: {})", article.getTitle(), article.getViewCount());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<NewsArticle> getNewsByCategory(String category) {
        log.info("카테고리별 뉴스 조회: {}", category);

        return Flux.fromIterable(newsRepository.values())
                .filter(article -> category.equalsIgnoreCase(article.getCategory()))
                .sort(Comparator.comparing(NewsArticle::getPublishedAt))
                .doOnNext(article -> log.debug("카테고리 [{}] 뉴스: {}", category, article.getTitle()));
    }

    public Flux<NewsArticle> getNewsStream() {
        log.info("실시간 뉴스 스트림 시작");

        return Flux.interval(Duration.ofSeconds(3))
                .map(tick -> generateRandomNews())
                .doOnNext(article -> {
                    newsRepository.put(article.getId(), article);
                    log.info("새 뉴스 생성: {}", article.getTitle());
                })
                .doOnCancel(() -> log.info("뉴스 스트림 종료"))
                .onBackpressureBuffer(50);
    }

    public Flux<NewsArticle> getPersonalizedNews(List<String> preferredCategories) {
        log.info("개인화 뉴스 조회: 선호 카테고리={}", preferredCategories);

        return getAllNews()
                .filter(article -> preferredCategories.contains(article.getCategory()))
                .take(10)
                .doOnNext(article -> log.debug("개인화 뉴스: {}", article.getTitle()));
    }

    public Flux<NewsArticle> searchNews(String keyword) {
        log.info("뉴스 검색: 키워드={}", keyword);

        return Flux.fromIterable(newsRepository.values())
                .filter(article ->
                        article.getTitle().toLowerCase().contains(keyword.toLowerCase())
                                || article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .sort(Comparator.comparing(NewsArticle::getPublishedAt).reversed())
                .doOnNext(article -> log.debug("검색 결과: {}", article.getTitle()));
    }

    public Flux<NewsArticle> getPopularNews(int limit) {
        log.info("인기 뉴스 조회: 상위 {}개", limit);

        return Flux.fromIterable(newsRepository.values())
                .sort(Comparator.comparing(NewsArticle::getViewCount).reversed())
                .take(limit)
                .doOnNext(article -> log.debug("인기 뉴스: {} (조회수: {})",
                        article.getTitle(), article.getViewCount()));
    }

    public Mono<NewsArticle> createNews(NewsArticle newsArticle) {
        log.info("새 뉴스 생성 요청: {}", newsArticle.getTitle());

        return Mono.fromSupplier(() -> {
            Long id = System.currentTimeMillis();
            newsArticle.setId(id);
            newsArticle.setPublishedAt(LocalDateTime.now());
            newsArticle.setViewCount(0);

            newsRepository.put(id, newsArticle);

            log.info("뉴스 생성 완료: ID={}", id);
            return newsArticle;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private NewsArticle generateRandomNews() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String category = categories.get(random.nextInt(categories.size()));
        String author = authors.get(random.nextInt(authors.size()));
        String title = String.format("[%s] %s의 새로운 소식 %d", category, author, System.currentTimeMillis() % 1000);
        String content = String.format("%s 카테고리의 상세한 뉴스 내용입니다. " + "이 뉴스는 %s 기자가 작성했습니다.", category, author);

        return NewsArticle.builder()
                .id(System.currentTimeMillis())
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .publishedAt(LocalDateTime.now())
                .viewCount(0)
                .build();
    }

    private void initializeData() {
        log.info("초기 뉴스 데이터 생성 시작");

        List<NewsArticle> initialNews = Arrays.asList(
                NewsArticle.create("Spring WebFlux 완전정복", "리액티브 프로그래밍의 새로운 패러다임", "TECH", "김기자"),
                NewsArticle.create("월드컵 결승전 하이라이트", "역사상 가장 치열했던 경기", "SPORTS", "이기자"),
                NewsArticle.create("새로운 정책 발표", "국민들의 관심이 집중되고 있습니다", "POLITICS", "박기자"),
                NewsArticle.create("블록버스터 영화 개봉", "올해 최고의 작품으로 평가받고 있습니다", "ENTERTAINMENT", "최기자"),
                NewsArticle.create("획기적인 과학적 발견", "노벨상 수상이 유력한 연구 결과", "SCIENCE", "정기자")
        );

        for (int i = 0; i < initialNews.size(); i++) {
            NewsArticle article = initialNews.get(i);
            article.setId((long) (i + 1));
            article.setViewCount(ThreadLocalRandom.current().nextInt(100, 1000));
            newsRepository.put(article.getId(), article);
        }

        log.info("초기 뉴스 데이터 {} 개 생성 완료", initialNews.size());
    }
}