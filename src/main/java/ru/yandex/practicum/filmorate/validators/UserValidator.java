package ru.yandex.practicum.filmorate.validators;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.HashSet;

public class UserValidator {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserValidator.class);

    public static void userValidator(@Valid User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не может быть null");
        }

        if (user.getBirthday().isAfter(ChronoLocalDate.from(LocalDate.now()))) {
            ValidationException valid = new ValidationException("Дата рождения не может быть из будущего");
            log.warn("Дата рождения не может быть из будущего", valid);
            throw valid;
        }

        if (user.getLogin().contains(" ")) {
            ValidationException valid = new ValidationException("логин не может содержать пробелы");
            log.warn("логин не может содержать пробелы", valid);
            throw valid;
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("У пользователя {} вместо имени используется login: {}", user, user.getLogin());
        }
    }
}
