package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /*
     * Работа с пользователем
     */

    @GetMapping
    public Collection<User> findAll() {
        return userService.inMemoryUserStorage.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.inMemoryUserStorage.findById(id);
    }

    @PostMapping
    public User create(@RequestBody User user) throws ValidationException {
        return userService.inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) throws ValidationException {
        return userService.inMemoryUserStorage.update(newUser);
    }

    @DeleteMapping
    public User delete(@RequestBody Long id) throws ValidationException {
        return userService.inMemoryUserStorage.delete(id);
    }

    /*
     * Работа с друзьями
     */

    @GetMapping("/{id}/friends")
    public List<User> findFriends(@PathVariable Long id) throws NotFoundException {
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findMutualFriends(@PathVariable Long id,
                                        @PathVariable Long otherId) throws NotFoundException {
        return userService.findMutualFriends(id, otherId);
    }

    @PutMapping("/{id}/friends/{friendsId}")
    public Set<Long> addFriend(@PathVariable Long id,
                               @PathVariable Long friendsId) throws NotFoundException {
        return userService.addFriend(id, friendsId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public Set<Long> deleteFriend(@PathVariable Long id,
                                  @PathVariable Long friendId) throws NotFoundException {
        return userService.deleteFriend(id, friendId);
    }
}
