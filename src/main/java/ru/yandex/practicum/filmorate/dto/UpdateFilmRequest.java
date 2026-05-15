package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UpdateFilmRequest {
    private Long id;
    private List<Genre> genres;
    private MPA mpa;
    private List<Director> directors;
    private String name;
    private String description;
    private LocalDate releaseDate = LocalDate.now();
    private Double duration;

    public boolean hasDirectors() {
        return directors != null;
    }

    public boolean hasGenres() {
        return genres != null;
    }

    public boolean hasMPA() {
        return mpa != null;
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null && duration > 0;
    }
}