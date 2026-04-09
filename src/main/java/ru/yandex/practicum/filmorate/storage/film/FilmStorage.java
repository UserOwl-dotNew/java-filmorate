package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film) throws InternalServerException;

    Film delete(Long id);

    Film update(Film newFilm) throws InternalServerException;
}
