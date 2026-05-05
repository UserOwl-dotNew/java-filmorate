package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
