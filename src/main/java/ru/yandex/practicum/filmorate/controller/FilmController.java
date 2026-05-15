package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    /*
     * Работа с фильмами
     */

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findAll() {
        log.info("GET /films - запрос на получение всех фильмов");
        Collection<FilmDto> films = filmService.findAll();
        log.info("GET /films - успешно получено {} фильмов", films.size());
        return films;

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto findById(@PathVariable("id") Long id) {
        log.info("GET /films/{} - запрос на получение фильма по id", id);
        FilmDto film = filmService.findFilmById(id);
        log.info("GET /films/{} - фильм успешно найден: {}", id, film.getName());
        return film;
    }

    @GetMapping("/director/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findAllFilmsByDirector(@PathVariable("directorId") Long directorId,
                                                      @RequestParam String sortBy) {
        log.info("GET /films/director/{}?sortBy={} - запрос на получение фильмов режиссера", directorId, sortBy);
        Collection<FilmDto> films = filmService.findFilmByDirector(directorId, sortBy);
        log.info("GET /films/director/{}?sortBy={} - успешно получено {} фильмов", directorId, sortBy, films.size());
        return films;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@RequestBody NewFilmRequest request) throws ValidationException, InternalServerException {
        log.info("POST /films - запрос на создание фильма с названием: {}", request.getName());
        FilmDto createdFilm = filmService.create(request);
        log.info("POST /films - фильм успешно создан с id: {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto update(@RequestBody UpdateFilmRequest request) throws InternalServerException {
        log.info("PUT /films - запрос на обновление фильма с id: {}", request.getId());
        FilmDto updatedFilm = filmService.update(request);
        log.info("PUT /films - фильм с id: {} успешно обновлен", updatedFilm.getId());
        return updatedFilm;
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public FilmDto delete(@PathVariable("filmId") Long id) throws ValidationException {
        log.info("DELETE /films/{} - запрос на удаление фильма", id);
        FilmDto deletedFilm = filmService.delete(id);
        log.info("DELETE /films/{} - фильм успешно удален", id);
        return deletedFilm;
    }

    /*
     * Поиск фильмов по названию и описанию
     */

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> search(@RequestParam String query, @RequestParam List<String> by) {
        log.info("GET /films/search?query={}&by={} - запрос на поиск фильмов", query, by);
        List<FilmDto> searchResults = filmService.search(query, by);
        log.info("GET /films/search?query={}&by={} - найдено {} фильмов", query, by, searchResults.size());
        return searchResults;
    }

    /*
     * Работа с лайками и выводом лучших фильмов
     */

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> findPopularFilms(
            @RequestParam(defaultValue = "10", required = false)
            @Min(value = 1, message = "Значение count должно быть положительным")
            Long count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) throws NotFoundException {
        log.info("GET /films/popular?count={}&genreId={}&year={} - запрос на получение популярных фильмов",
                count, genreId, year);
        List<FilmDto> popularFilms = filmService.findPopularFilm(count, genreId, year);
        log.info("GET /films/popular - успешно получено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void setLike(@PathVariable Long id,
                        @PathVariable Long userId) throws NotFoundException, InternalServerException {
        log.info("PUT /films/{}/like/{} - запрос на добавление лайка фильму", id, userId);
        filmService.like(id, userId);
        log.info("PUT /films/{}/like/{} - лайк успешно добавлен", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void setDislike(@PathVariable Long id,
                           @PathVariable Long userId) throws NotFoundException {
        log.info("DELETE /films/{}/like/{} - запрос на удаление лайка у фильма", id, userId);
        filmService.disLike(id, userId);
        log.info("DELETE /films/{}/like/{} - лайк успешно удален", id, userId);
    }

    @GetMapping("/common")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> findCommonFilms(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        log.info("GET /films/common?userId={}&friendId={} - запрос на получение общих фильмов", userId, friendId);
        List<FilmDto> commonFilms = filmService.findCommonFilms(userId, friendId);
        log.info("GET /films/common - найдено {} общих фильмов", commonFilms.size());
        return commonFilms;
    }
}