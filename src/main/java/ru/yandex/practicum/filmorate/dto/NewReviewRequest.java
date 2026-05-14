package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewReviewRequest {
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
}