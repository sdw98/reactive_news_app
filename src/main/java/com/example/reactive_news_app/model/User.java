package com.example.reactive_news_app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {

    @Id
    private Long id;

    private String username;
    private String email;
    private List<String> preferredCategories;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}