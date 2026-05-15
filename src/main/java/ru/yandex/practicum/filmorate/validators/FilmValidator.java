package ru.yandex.practicum.filmorate.validators;

import jakarta.validation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Slf4j
public class FilmValidator {
    public static void filmValidator(@Valid Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            ValidationException valid = new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            log.warn("Дата релиза — не раньше 28 декабря 1895 года", valid);
            throw valid;
        }

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Validation faild", violations);
        }

        if (film.getDuration() <= 0) {
            ValidationException valid = new ValidationException("Продолжительность фильма должна быть положительным числом");
            log.warn("Продолжительность фильма должна быть положительным числом", valid);
            throw valid;
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может превышать 200 символов");
        }

        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}
