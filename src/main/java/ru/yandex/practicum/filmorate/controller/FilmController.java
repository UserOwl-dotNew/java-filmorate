package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    /*
     * Работа с фильмами
     */

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto findById(@PathVariable("id") Long id) {
        return filmService.findFilmById(id);
    }

    @GetMapping("/director/{directorId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findAllFilmsByDirector(@PathVariable("directorId") Long directorId,
                                                      @RequestParam String sortBy) {
        return filmService.findFilmByDirector(directorId, sortBy);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@RequestBody NewFilmRequest request) throws ValidationException, InternalServerException {
        return filmService.create(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto update(@RequestBody UpdateFilmRequest request) throws InternalServerException {
        return filmService.update(request);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public FilmDto delete(@PathVariable("filmId") Long id) throws ValidationException {
        return filmService.delete(id);
    }

    /*
     * Работа с лайками и выводом лучших фильмов
     */

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> findPopularFilms(
            @RequestParam(defaultValue = "10", required = false) Long count) throws NotFoundException {
        if (count <= 0) {
            log.warn("Значение count должно быть положительным");
            throw new ValidationException("Значение count должно быть положительным");
        }
        return filmService.findPopularFilm(count);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Long setLike(@PathVariable Long id,
                        @PathVariable Long userId) throws NotFoundException, InternalServerException {
        return filmService.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Long setDislike(@PathVariable Long id,
                           @PathVariable Long userId) throws NotFoundException {
        return filmService.disLike(id, userId);
    }
}
