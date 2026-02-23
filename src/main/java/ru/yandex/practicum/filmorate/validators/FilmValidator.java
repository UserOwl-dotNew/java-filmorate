package ru.yandex.practicum.filmorate.validators;

import ch.qos.logback.classic.Logger;
import jakarta.validation.*;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

public class FilmValidator {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmValidator.class);

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
//        if(film.getName() == null || film.getName().isBlank()) {
//            ValidationException valid = new ValidationException("Название не может быть пустым");
//            log.warn("Не было передано название фильма", valid);
//            throw valid;
//        }

//        if(film.getDescription().length() > 200) {
//            ValidationException valid = new ValidationException("Максимальная длина описания - 200 символов");
//            log.warn("Максимальная длина описания - 200 символов", valid);
//            throw valid;
//        }

        if (film.getDuration() <= 0) {
            ValidationException valid = new ValidationException("Продолжительность фильма должна быть положительным числом");
            log.warn("Продолжительность фильма должна быть положительным числом", valid);
            throw valid;
        }
    }
}
