package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setMpa(request.getMpa());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            List<Genre> genres = request.getGenres();
            film.setGenres(genres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if(film.getMpa() != null) {
            MPADto mpaDto = new MPADto();
            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());
            dto.setMpa(mpaDto);
        }

        if(film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<GenreDto> genreDtos = film.getGenres().stream()
                    .map(GenreMapper::mapToGenreDto)
                    .toList();
            dto.setGenres(genreDtos);
        } else {
            dto.setGenre(Collections.emptyList());
        }

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }

        if (request.hasMPA()) {
            film.setMpa(request.getMpa());
        }

        if (request.hasName()) {
            film.setName(request.getName());
        }

        if (request.hasGenres()) {
            film.setGenres(request.getGenres());
        }

        return film;
    }
}

