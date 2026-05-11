package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
public class ReviewReaction {
    private Long id;
    private LocalDateTime createdAt;
    private Long userId;
    private Long reviewId;
    private String reaction_type;

}
