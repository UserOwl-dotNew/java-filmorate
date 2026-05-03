package ru.yandex.practicum.filmorate.services;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MPAMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmService.class);
    private final FilmDbStorage filmDbStorage;
    private final LikeDbStorage likeDbStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    public FilmDto update(UpdateFilmRequest request) throws InternalServerException {
        Film updateFilm = filmDbStorage.findById(request.getId())
                .map(film -> FilmMapper.updateFilmFields(film, request))
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
        FilmValidator.filmValidator(updateFilm);
        return FilmMapper.mapToFilmDto(updateFilm);
    }

    public FilmDto delete(Long id) {
        return FilmMapper.mapToFilmDto(filmDbStorage.delete(id));
    }

    public FilmDto create(NewFilmRequest request) throws InternalServerException {
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            MPA existingMpa = mpaStorage.findById(request.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA с id: " + request.getMpa().getId() + " не найден"));
            request.getMpa().setName(existingMpa.getName());
        } else {
            throw new ValidationException("MPA рейтинг должен быть указан");
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            for (Genre genre : request.getGenres()) {
                Long genreId = genre.getId();
                if (genreId != null) {
                    Genre existingGenre = genreStorage.findById(genreId)
                            .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
                }
            }
        }

        Film film = FilmMapper.mapToFilm(request);
        FilmValidator.filmValidator(film);
        film = filmDbStorage.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> findAll() {
        return filmDbStorage.findAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public Long like(Long filmId, Long userId) throws InternalServerException {
        filmDbStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
        return likeDbStorage.create(userId, filmId);
    }

    public Long disLike(Long userId, Long filmId) {
        return likeDbStorage.delete(userId, filmId);
    }

    public List<FilmDto> findPopularFilm(Long count, Long genreId, Integer year) {
        return filmDbStorage.findPopular(count, genreId, year)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public List<GenreDto> findAllGenres() {
        return genreStorage.findAll()
                .stream()
                .map(GenreMapper::mapToGenreDto)
                .toList();
    }

    public GenreDto findGenreById(Long id) {
        Genre genre = genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
        return GenreMapper.mapToGenreDto(genreStorage.findById(id).get());
    }

    public List<MPADto> findAllMPA() {
        return mpaStorage.findAll()
                .stream()
                .map(MPAMapper::mapToMPADto)
                .toList();
    }

    public MPADto findMPAById(Long id) {
        MPA mpa = mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA с id " + id + " не найден"));
        return MPAMapper.mapToMPADto(mpaStorage.findById(id).get());
    }

    public FilmDto findFilmById(Long id) {
        Film film = filmDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }
}
