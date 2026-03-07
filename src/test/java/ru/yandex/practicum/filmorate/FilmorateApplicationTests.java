package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.FilmValidator;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static java.time.ZoneOffset.UTC;

@SpringBootTest
class FilmorateApplicationTests {
    // Тесты FilmValidator
    @Test
    void filmValidator_shouldThrowValidationException_whenNameIsBlank() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film film = new Film();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    void filmValidator_shouldThrowValidationException_whenMaxLengthOfDescriptionMore200() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        String str = ".".repeat(201);
        Film film = new Film();
        film.setDescription(str);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    void filmValidator_shouldThrowValidationException_whenDateReleaseIsBefore28_12_1895() {
        LocalDate localDate = LocalDate.of(1895, Month.DECEMBER, 27);
        LocalDate localDateBeforeOld = LocalDate.of(1895, Month.DECEMBER, 27);
        Film film = new Film();
        film.setReleaseDate(localDate);

        Assertions.assertThrows(ValidationException.class, () -> {
            FilmValidator.filmValidator(film);
        });

        film.setReleaseDate(localDateBeforeOld);
        Assertions.assertThrows(ValidationException.class, () -> {
            FilmValidator.filmValidator(film);
        });
    }

    @Test
    void filmValidator_shouldThrowValidationException_whenDurationIsNegative() {
        Film film = new Film();
        film.setDuration(0L);

        Assertions.assertThrows(NullPointerException.class, () -> {
            FilmValidator.filmValidator(film);
        });

        film.setDuration(-1L);
        Assertions.assertThrows(NullPointerException.class, () -> {
            FilmValidator.filmValidator(film);
        });
    }

    @Test
    void filmValidator_shouldThrowValidationException_whenRequestIsBlank() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film film = new Film();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty());
    }

    // Тесты UserValidator
    @Test
    void userValidator_shouldThrowValidationException_whenUserEmailIsBlank() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User user = new User();
        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());

        user.setEmail("dmitri.yandex.ru");
        violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    void userValidator_shouldThrowValidationException_whenUserEmailNotContainsAt() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User user = new User();
        user.setEmail("dmitri.yandex.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    void userValidator_shouldThrowValidationException_whenUserLoginIsBlank() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User user = new User();
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    void userValidator_shouldThrowValidationException_whenUserLoginIsContainsSpaces() {
        User user = new User();
        user.setLogin("d  oo m");

        Assertions.assertThrows(ValidationException.class, () -> {
            UserValidator.userValidator(user);
        });
    }

    @Test
    void userValidator_shouldThrowValidationException_whenBirthdayUserInFuture() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        LocalDate birthdayInFuture = LocalDate.ofInstant(Instant.now(), UTC.normalized()).plusDays(1);
        User user = new User();
        user.setBirthday(birthdayInFuture);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        Assertions.assertFalse(violations.isEmpty());
    }
}
