package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewReaction {
    private Long id;
    private LocalDateTime createdAt;
    private Long userId;
    private Long reviewId;
    private String reactionType;

}
