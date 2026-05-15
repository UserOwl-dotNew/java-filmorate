package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.services.EventService;
import ru.yandex.practicum.filmorate.services.FilmService;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    /*
     * Работа с пользователем
     */

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> findAll() {
        log.info("GET /users - запрос на получение всех пользователей");
        Collection<UserDto> users = userService.getUsers();
        log.info("GET /users - успешно получено {} пользователей", users.size());
        return users;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable Long id) {
        log.info("GET /users/{} - запрос на получение пользователя по id", id);
        UserDto user = userService.getUserById(id);
        log.info("GET /users/{} - пользователь успешно найден: {}", id, user.getEmail());
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody NewUserRequest request) throws ValidationException, InternalServerException {
        log.info("POST /users - запрос на создание пользователя с email: {}", request.getEmail());
        UserDto createdUser = userService.createUser(request);
        log.info("POST /users - пользователь успешно создан с id: {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@RequestBody UpdateUserRequest request) throws InternalServerException {
        log.info("PUT /users - запрос на обновление пользователя с id: {}", request.getId());
        UserDto updatedUser = userService.updateUser(request);
        log.info("PUT /users - пользователь с id: {} успешно обновлен", updatedUser.getId());
        return updatedUser;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto update(@PathVariable Long id, @RequestBody UpdateUserRequest request) throws ValidationException, InternalServerException {
        log.info("PUT /users/{} - запрос на обновление пользователя", id);
        UserDto updatedUser = userService.updateUser(id, request);
        log.info("PUT /users/{} - пользователь успешно обновлен", id);
        return updatedUser;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserDto delete(@PathVariable("userId") Long id) throws ValidationException, InternalServerException {
        log.info("DELETE /users/{} - запрос на удаление пользователя", id);
        UserDto deletedUser = userService.deleteUser(id);
        log.info("DELETE /users/{} - пользователь успешно удален", id);
        return deletedUser;
    }

    /*
     * Работа с друзьями
     */

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findFriends(@PathVariable Long id) throws NotFoundException {
        log.info("GET /users/{}/friends - запрос на получение списка друзей пользователя", id);
        List<UserDto> friends = userService.getFriends(id);
        log.info("GET /users/{}/friends - успешно получено {} друзей", id, friends.size());
        return friends;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findMutualFriends(@PathVariable Long id,
                                           @PathVariable Long otherId) throws NotFoundException {
        log.info("GET /users/{}/friends/common/{} - запрос на получение общих друзей", id, otherId);
        List<UserDto> mutualFriends = userService.getMutualFriends(id, otherId);
        log.info("GET /users/{}/friends/common/{} - найдено {} общих друзей", id, otherId, mutualFriends.size());
        return mutualFriends;
    }

    @PutMapping("/{id}/friends/{friendsId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> addFriend(@PathVariable Long id,
                                   @PathVariable Long friendsId) throws NotFoundException, InternalServerException {
        log.info("PUT /users/{}/friends/{} - запрос на добавление друга", id, friendsId);
        List<UserDto> friends = userService.addFriend(id, friendsId);
        log.info("PUT /users/{}/friends/{} - друг успешно добавлен", id, friendsId);
        return friends;
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public List<UserDto> deleteFriend(@PathVariable Long id,
                                      @PathVariable Long friendId) throws NotFoundException, InternalServerException {
        log.info("DELETE /users/{}/friends/{} - запрос на удаление друга", id, friendId);
        List<UserDto> friends = userService.deleteFriend(id, friendId);
        log.info("DELETE /users/{}/friends/{} - друг успешно удален", id, friendId);
        return friends;
    }

    /*
     * рекомендации по фильмам
     */
    @GetMapping("{id}/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getRecommendationsFilms(@PathVariable("id") Long id) {
        log.info("GET /users/{}/recommendations - запрос на получение рекомендаций фильмов", id);
        List<FilmDto> recommendations = filmService.findRecommendationsFilms(id);
        log.info("GET /users/{}/recommendations - получено {} рекомендаций", id, recommendations.size());
        return recommendations;
    }

    /*
     * Лента событий пользователя
     */
    @GetMapping("/{id}/feed")
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getUserFeed(@PathVariable("id") Long id) {
        log.info("GET /users/{}/feed - запрос на получение ленты событий пользователя", id);
        List<EventDto> feed = eventService.getUserFeed(id);
        log.info("GET /users/{}/feed - получено {} событий", id, feed.size());
        return feed;
    }
}