package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setMpa(request.getMpa());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        film.setGenres(request.getGenres() != null ? request.getGenres() : new ArrayList<>());
        film.setDirectors(request.getDirectors() != null ? request.getDirectors() : new ArrayList<>());

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
        } else {
            film.setDirectors(new ArrayList<>());
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