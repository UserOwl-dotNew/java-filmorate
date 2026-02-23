package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

/**
 * Film.
 */
@Getter
@Setter
@Data
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @Length(max = 200)
    String description;
    LocalDate releaseDate;
    Long duration;
}
