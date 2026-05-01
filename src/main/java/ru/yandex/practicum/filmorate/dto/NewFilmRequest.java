package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class NewFilmRequest {
    private Long id;
    private List<Genre> genres;
    private MPA mpa;
    private List<Director> directors;
    private String name;
    private String description;
    private Double duration;
    private LocalDate releaseDate;
}
