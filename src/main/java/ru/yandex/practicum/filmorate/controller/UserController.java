package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> finalAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        UserValidator.userValidator(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        UserValidator.userValidator(newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getLogin() == null || newUser.getLogin().isBlank()) {
                oldUser.setLogin(newUser.getLogin());
                log.debug("Установлено новое значение login: {} для пользователя: {}", newUser.getLogin(), newUser);
            }
            if (!(newUser.getName() == null || newUser.getName().isBlank())) {
                oldUser.setName(newUser.getName());
                log.debug("Установлено новое значение name: {} для пользователя: {}", newUser.getName(), newUser);
            }
            if (!(newUser.getBirthday() == null)) {
                oldUser.setBirthday(newUser.getBirthday());
                log.debug("Установлено новое значение birthday: {} для пользователя: {}", newUser.getBirthday(), newUser);
            }
            log.info("Данные о пользователе {} обновлены", oldUser);
            return oldUser;
        }
        log.warn("Пользователя с таким id: {} не найден", +newUser.getId());
        throw new NotFoundException("Пользователя с таким id: " + newUser.getId() + " не найден");
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
