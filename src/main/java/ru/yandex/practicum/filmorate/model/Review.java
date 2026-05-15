package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long reviewId;

    @NotNull(message = "Содержание не может быть null")
    @NotBlank(message = "Содержание не может быть пустым")
    @Length(max = 200, message = "Максимальная длина содержания 200 символов")
    private String content;

    private LocalDateTime createdAt;

    @NotNull(message = "Поле isPositive обязательно")
    private Boolean isPositive;

    private Integer useful;

    @NotNull(message = "ID фильма обязателен")
    private Long filmId;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;

    private Integer likesCount;
    private Integer dislikesCount;
}