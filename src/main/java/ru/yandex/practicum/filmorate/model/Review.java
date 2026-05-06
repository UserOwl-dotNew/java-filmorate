package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Review {
    private Long id;
    //private List<ReviewReaction> reactions;
    @NotNull
    @NotBlank
    @Length(max = 200)
    private String content;

    private LocalDateTime createdAt;
    private Boolean isPositive;
    private Integer useful;
    private Long filmId;
    private Long userId;


    public List<String> validateErrors() {
        List<String> errors = new ArrayList<>();

        if (content == null || content.isBlank() || content.isEmpty()) {
            errors.add("Содержание не может быть пустым.");
        }

        if (content != null && content.length() > 200) {
            errors.add("Максимальная длина содержания 200 символов.");
        }

        return errors;
    }
}
