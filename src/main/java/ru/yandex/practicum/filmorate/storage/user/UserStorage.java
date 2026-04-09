package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();

    User create(User user) throws InternalServerException;

    User update(User newUser) throws InternalServerException;

    User delete(Long id) throws InternalServerException;
}
