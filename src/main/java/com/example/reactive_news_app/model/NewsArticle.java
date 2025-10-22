package com.example.reactive_news_app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("news_articles")
public class NewsArticle {
    @Id
    private Locked id;

    private String title;
    private String content;
    private String category;
    private String author;
    private LocalDateTime publishedAt;
    private Integer viewCount;
    private List<String> tags;

    public static NewsArticle create(String title, String content, String category, String author) {
        return NewsArticle.builder()
                .title(title)
                .content(content)
                .category(category)
                .author(author)
                .publishedAt(LocalDateTime.now())
                .viewCount(0)
                .build();
    }
}
