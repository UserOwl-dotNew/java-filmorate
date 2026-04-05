package ru.yandex.practicum.filmorate.storage.film;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private static final Map<Long, Film> films = new HashMap<>();

    public List<Film> findAll() {
        return films.values()
                .stream()
                .toList();
    }

    public Film create(Film film) {
        FilmValidator.filmValidator(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film);
        return film;
    }

    public Film delete(Long id) {
        if (films.containsKey(id)) {
            log.info("Фильм {} успешно удален", films.get(id));
            return films.remove(id);
        }
        throw new NotFoundException("Пост с id: " + id + " не найден");
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Не был указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }
        log.info("Фильм успешно обновлен");

        FilmValidator.filmValidator(newFilm);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setDescription(newFilm.getDescription());
            log.info("Фильм {} с id: {} обновлен", newFilm, newFilm.getId());
            return oldFilm;
        }
        log.warn("Пост с id: {} не найден", newFilm.getId());
        throw new NotFoundException("Пост с id: " + newFilm.getId() + " не найден");
    }

    public static Optional<Film> findById(Long id) {
        Optional<Film> findFilm = films.values().stream().filter(film -> id.equals(film.getId())).findFirst();
        if (findFilm.isPresent()) {
            return findFilm;
        }
        log.warn("Фильм с id: {} не найден", id);
        throw new NotFoundException("Фильм с id: " + id + " не найден");
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