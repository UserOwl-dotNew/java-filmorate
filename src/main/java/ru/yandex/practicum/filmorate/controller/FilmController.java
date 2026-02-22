package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ValidationException {
        FilmValidator.filmValidator(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) throws ValidationException {
        if (newFilm.getId() == null) {
            log.warn("Не был указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        FilmValidator.filmValidator(newFilm);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setDescription(newFilm.getDescription());
            log.info("Фильм {} с id: {} обновлен", newFilm, newFilm.getId());
            return oldFilm;
        }
        log.warn("Пост с id: {} не найден", newFilm.getId());
        throw new NotFoundException("Пост с id: " + newFilm.getId() + " не найден");
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
