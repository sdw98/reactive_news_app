package com.example.reactive_news_app.config;

import com.example.reactive_news_app.handler.NewsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@Slf4j
public class WebFluxConfig {
    @Bean
    public RouterFunction<ServerResponse> newsRoutes(NewsHandler newsHandler) {
        log.info("함수형 라우팅 설정 초기화");

        return RouterFunctions
                .route(GET("/functional/news").and(accept(MediaType.APPLICATION_JSON)), newsHandler::getNewsById)
                .andRoute(GET("/functional/news/{id}").and(accept(MediaType.APPLICATION_JSON)), newsHandler::getNewsById)
                .andRoute(POST("/functional/news").and(contentType(MediaType.APPLICATION_JSON)), newsHandler::createNews)
                .andRoute(GET("/functional/news/stream").and(accept(MediaType.TEXT_EVENT_STREAM)), newsHandler::getNewsStream)
                .andRoute(GET("/functional/news/category/{category}").and(accept(MediaType.APPLICATION_JSON)), newsHandler::getNewsByCategory);
    }
}