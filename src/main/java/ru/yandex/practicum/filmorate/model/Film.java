package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @Length(max = 200)
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Set<Long> likes;
    private Long countLikes;
}
