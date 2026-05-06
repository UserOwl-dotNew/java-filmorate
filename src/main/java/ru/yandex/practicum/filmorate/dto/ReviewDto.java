package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String content;
    private Boolean is_positive;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime created_at = LocalDateTime.now();
    private Integer useful;
    //private List<ReviewReactionDto> review_reactions;
}
