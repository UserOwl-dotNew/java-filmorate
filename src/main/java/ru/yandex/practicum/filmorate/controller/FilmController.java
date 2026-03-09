package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    /*
     * Работа с фильмами
     */

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) throws ValidationException {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) throws ValidationException {
        return filmService.update(newFilm);
    }

    @DeleteMapping
    public Film delete(@RequestBody Long id) throws ValidationException {
        return filmService.delete(id);
    }

    /*
     * Работа с лайками и выводом лучших фильмов
     */

    @GetMapping("/popular")
    public List<Film> findPopularFilm(
            @RequestParam(defaultValue = "10", required = false) Long count) throws NotFoundException {
        if (count <= 0) {
            log.warn("Значение count должно быть положительным");
            throw new ValidationException("Значение count должно быть положительным");
        }
        return filmService.findPopularFilms(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Set<Long> setLike(@PathVariable Long id,
                             @PathVariable Long userId) throws NotFoundException {
        return filmService.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Set<Long> setDislike(@PathVariable Long id,
                                @PathVariable Long userId) throws NotFoundException {
        return filmService.disLike(id, userId);
    }
}
