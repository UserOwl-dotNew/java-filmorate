package ru.yandex.practicum.filmorate.services;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService implements FilmStorage {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmService.class);
    private final InMemoryFilmStorage inMemoryFilmStorage;

    @Override
    public Film update(Film newFilm) {
        return inMemoryFilmStorage.update(newFilm);
    }

    @Override
    public Film delete(Long id) {
        return inMemoryFilmStorage.delete(id);
    }

    @Override
    public Film create(Film film) {
        return inMemoryFilmStorage.create(film);
    }

    @Override
    public List<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Set<Long> like(Long filmId, Long userId) throws NotFoundException {
        User user = InMemoryUserStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + userId + " не найден"));

        Film film = InMemoryFilmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден"));

        FilmValidator.filmValidator(film);
        Set<Long> likesFilm = film.getLikes();
        if (!likesFilm.contains(userId)) {
            log.info("Лайк на фильм с id: {}, успешно поставлен пользователем: {}", filmId, user);
            likesFilm.add(userId);
            film.setCountLikes(film.getCountLikes() + 1);
        }

        return likesFilm;
    }

    public Set<Long> disLike(Long filmId, Long userId) throws NotFoundException {
        User user = InMemoryUserStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + userId + " не найден"));

        Film film = InMemoryFilmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден"));

        FilmValidator.filmValidator(film);
        Set<Long> likesFilm = film.getLikes();
        if (likesFilm.contains(userId)) {
            log.info("Лайк на фильм с id: {}, успешно удалён пользователем: {}", filmId, user);
            likesFilm.remove(userId);
            film.setCountLikes(film.getCountLikes() - 1);
        }
        return likesFilm;
    }

    public List<Film> findPopularFilms(Long count) {
        return inMemoryFilmStorage.findAll()
                .stream()
                .sorted((f1, f2) -> {
                    return Long.compare(
                            f2.getLikes().size(),
                            f1.getLikes().size()
                    );
                })
                .limit(count)
                .toList();
    }
}
