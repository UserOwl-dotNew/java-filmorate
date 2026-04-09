package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
//    private final Fri

    /*
     * Работа с пользователем
     */

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> findAll() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody NewUserRequest request) throws ValidationException, InternalServerException {
        return userService.createUser(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto update(@RequestBody Long id, UpdateUserRequest request) throws ValidationException {
        return userService.updateUser(id, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserDto delete(@RequestBody Long id) throws ValidationException, InternalServerException {
        return userService.deleteUser(id);
    }

    /*
     * Работа с друзьями
     */

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findFriends(@PathVariable Long id) throws NotFoundException {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findMutualFriends(@PathVariable Long id,
                                        @PathVariable Long otherId) throws NotFoundException {
        return userService.getMutualFriends(id, otherId);
    }

    @PutMapping("/{id}/friends/{friendsId}")
    @ResponseStatus(HttpStatus.UPGRADE_REQUIRED)
    public List<UserDto> addFriend(@PathVariable Long id,
                               @PathVariable Long friendsId) throws NotFoundException, InternalServerException {
        return userService.addFriend(id, friendsId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public List<UserDto> deleteFriend(@PathVariable Long id,
                                  @PathVariable Long friendId) throws NotFoundException, InternalServerException {
        return userService.deleteFriend(id, friendId);
    }
}
