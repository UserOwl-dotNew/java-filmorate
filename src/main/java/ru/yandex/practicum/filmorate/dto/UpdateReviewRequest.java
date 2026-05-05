package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewRequest {
    private Long reviewId;
    private String content;
    @JsonProperty("isPositive")
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
}
