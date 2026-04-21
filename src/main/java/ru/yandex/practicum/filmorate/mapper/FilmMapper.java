package ru.yandex.practicum.filmorate.mapper;

import ch.qos.logback.classic.Logger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmMapper.class);

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

        if (request.getDirectors() != null && request.getDirectors().isEmpty()) {
            List<Director> directors = request.getDirectors();
            film.setDirectors(directors);
        } else {
            film.setDirectors(new ArrayList<>());
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

        if (film.getMpa() != null) {
            MPADto mpaDto = new MPADto();
            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());
            dto.setMpa(mpaDto);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<DirectorDto> directorDtos = film.getDirectors()
                    .stream()
                    .map(director -> {
                        log.info("Add new directorDto: ");
                        DirectorDto directorDto = new DirectorDto();
                        log.info("setId: " + director.getId());
                        directorDto.setId(director.getId());
                        log.info("setName: " + director.getName());
                        directorDto.setName(director.getName());
                        return directorDto;
                    })
                    .collect(Collectors.toList());
            dto.setDirectors(directorDtos);
        } else {
            dto.setDirectors(Collections.emptyList());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<GenreDto> genreDtos = film.getGenres().stream()
                    .map(genre -> {
                        GenreDto genreDto = new GenreDto();
                        genreDto.setId(genre.getId());
                        genreDto.setName(genre.getName());
                        return genreDto;
                    })
                    .collect(Collectors.toList());
            dto.setGenres(genreDtos);
        } else {
            dto.setGenres(Collections.emptyList());
        }

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasDirectors()) {
            log.info("request.hasDirectors(): {}", request.hasDirectors());
            film.setDirectors(request.getDirectors());
        }

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

        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }

        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        return film;
    }
}

