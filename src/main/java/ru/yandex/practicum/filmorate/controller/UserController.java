package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.services.EventService;
import ru.yandex.practicum.filmorate.services.FilmService;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

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
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@RequestBody UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest request) throws ValidationException {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserDto delete(@PathVariable("userId") Long id) throws ValidationException, InternalServerException {
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
    @ResponseStatus(HttpStatus.OK)
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

    /*
     * рекомендации по фильмам
     */
    @GetMapping("{id}/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getRecommendationsFilms(@PathVariable("id") Long id) {
        return filmService.findRecommendationsFilms(id);
    }

    /*
     * Лента событий пользователя
     */
    @GetMapping("/{id}/feed")
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getUserFeed(@PathVariable("id") Long id) {
        return eventService.getUserFeed(id);
    }
}
