package ru.yandex.practicum.filmorate.storage.user;

import ch.qos.logback.classic.Logger;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserStorage implements UserStorage {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(InMemoryUserStorage.class);
    private static final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        return users.values()
                .stream()
                .toList();
    }

    @Override
    public User create(@Valid User user) {
        log.info("Пользователь {} Готовиться к проверке", user);
        UserValidator.userValidator(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        UserValidator.userValidator(newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());

            log.info("Данные о пользователе {} обновлены", oldUser);
            return oldUser;
        }
        log.warn("Пользователя с таким id: {} не найден", newUser.getId());
        throw new NotFoundException("Пользователя с таким id: " + newUser.getId() + " не найден");
    }

    @Override
    public User delete(Long id) {
        if (users.containsKey(id)) {
            log.info("Пользователь {} удален", users.get(id));
            return users.remove(id);
        }
        throw new NotFoundException("Пользователя с таким id: " + id + " не найден");
    }

    public static Optional<User> findById(Long id) {
        Optional<User> findUser = users.values()
                .stream()
                .filter(user -> id.equals(user.getId()))
                .findFirst();
        if (findUser.isEmpty()) {
            log.warn("Пользователя с таким id: {} не найден", id);
            throw new NotFoundException("Пользователя с таким id: " + id + " не найден");
        }
        return findUser;
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
