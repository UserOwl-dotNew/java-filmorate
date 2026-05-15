package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {
    public static void userValidator(User user) {
        // Валидация email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Email должен быть указан");
            throw new ValidationException("Email должен быть указан");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Email должен содержать символ @");
            throw new ValidationException("Email должен содержать символ @");
        }

        // Валидация login
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Логин должен быть указан");
            throw new ValidationException("Логин должен быть указан");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Логин не может содержать пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        // Валидация birthday
        if (user.getBirthday() == null) {
            log.error("Дата рождения не может быть null");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть из будущего");
            throw new ValidationException("Дата рождения не может быть из будущего");
        }

        // Если name не указан, используем login
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("У пользователя {} вместо имени используется login: {}", user.getLogin(), user.getLogin());
        }
    }
}
