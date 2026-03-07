package ru.yandex.practicum.filmorate.validators;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

public class UserValidator {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserValidator.class);

    public static void userValidator(User user) {
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
