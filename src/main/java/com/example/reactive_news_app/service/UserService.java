package com.example.reactive_news_app.service;

import com.example.reactive_news_app.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    private final Map<Long, User> userRepository = new HashMap<>();

    public UserService() {
        initializeUsers();
    }

    public Flux<User> getAllUsers() {
        log.info("모든 사용자 조회 요청");

        return Flux.fromIterable(userRepository.values())
                .doOnNext(user -> log.debug("사용자 반환: {}", user.getUsername()));
    }

    public Mono<User> getUserById(Long id) {
        log.info("사용자 조회 요청: ID={}", id);

        return Mono.fromSupplier(() -> userRepository.get(id))
                .switchIfEmpty(Mono.error(new RuntimeException("사용자를 찾을 수 없습니다: " + id)))
                .doOnNext(user -> {
                    // 마지막 활동 시간 업데이트
                    user.setLastActiveAt(LocalDateTime.now());
                    log.info("사용자 조회 완료: {}", user.getUsername());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> createUser(User user) {
        log.info("새 사용자 생성 요청: {}", user.getUsername());

        return Mono.fromSupplier(() -> {
            Long id = System.currentTimeMillis();
            user.setId(id);
            user.setCreatedAt(LocalDateTime.now());
            user.setLastActiveAt(LocalDateTime.now());

            userRepository.put(id, user);

            log.info("사용자 생성 완료: ID={}, 이름={}", id, user.getUsername());
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> getUserPreferences(Long userId) {
        log.info("사용자 선호도 조회: ID={}", userId);

        return getUserById(userId)
                .map(User::getPreferredCategories)
                .switchIfEmpty(Mono.just(Arrays.asList("TECH"))); // 기본값
    }

    private void initializeUsers() {
        log.info("초기 사용자 데이터 생성 시작");

        List<User> initialUsers = Arrays.asList(
                User.builder()
                        .id(1L)
                        .username("techuser")
                        .email("tech@example.com")
                        .preferredCategories(Arrays.asList("TECH", "SCIENCE"))
                        .createdAt(LocalDateTime.now())
                        .lastActiveAt(LocalDateTime.now())
                        .build(),

                User.builder()
                        .id(2L)
                        .username("sportsfan")
                        .email("sports@example.com")
                        .preferredCategories(Arrays.asList("SPORTS"))
                        .createdAt(LocalDateTime.now())
                        .lastActiveAt(LocalDateTime.now())
                        .build(),

                User.builder()
                        .id(3L)
                        .username("newsaddict")
                        .email("news@example.com")
                        .preferredCategories(Arrays.asList("POLITICS", "ENTERTAINMENT"))
                        .createdAt(LocalDateTime.now())
                        .lastActiveAt(LocalDateTime.now())
                        .build()
        );

        initialUsers.forEach(user -> userRepository.put(user.getId(), user));

        log.info("초기 사용자 데이터 {} 개 생성 완료", initialUsers.size());
    }
}