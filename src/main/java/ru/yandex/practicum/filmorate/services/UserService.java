package ru.yandex.practicum.filmorate.services;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.FriendsRequestDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserService.class);
    private final UserDbStorage userDbStorage;
    private final FriendsRequestDbStorage friendsRequestDbStorage;
    private final FilmDbStorage filmStorage;
    private final EventDbStorage eventDbStorage;

    public List<UserDto> getUsers() {
        return userDbStorage.findAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(NewUserRequest request) throws InternalServerException {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

        Optional<User> alreadyExistUser = userDbStorage.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new DuplicatedDataException("Данный имейл уже используется");
        }

        User user = UserMapper.mapToUser(request);
        UserValidator.userValidator(user);
        user = userDbStorage.create(user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto getUserById(long id) {
        return userDbStorage.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
    }

    public UserDto updateUser(long userId, UpdateUserRequest request) throws InternalServerException {
        User updateUser = userDbStorage.findById(userId)
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userDbStorage.update(updateUser);
        UserValidator.userValidator(updateUser);
        return UserMapper.mapToUserDto(updateUser);
    }

    public UserDto updateUser(UpdateUserRequest request) throws InternalServerException {
        User updateUser = userDbStorage.findById(request.getId())
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userDbStorage.update(updateUser);
        UserValidator.userValidator(updateUser);
        return UserMapper.mapToUserDto(updateUser);
    }

    public UserDto deleteUser(long userId) throws InternalServerException {
        userDbStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));
        return UserMapper.mapToUserDto(userDbStorage.delete(userId));
    }

    public List<UserDto> getFriends(Long id) {
        userDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + id));
        List<UserDto> userDtos = userDbStorage.findFriends(id)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
        return userDtos;
    }

    public List<UserDto> getMutualFriends(long fromUserId, long toUserId) {
        return userDbStorage.findMutualFriends(fromUserId, toUserId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> addFriend(long fromUserId, long toUserId) throws InternalServerException {
        Long from = getUserById(fromUserId).getId();
        Long to = getUserById(toUserId).getId();
        Long id = friendsRequestDbStorage.create(fromUserId, toUserId);
        eventDbStorage.addEvent(fromUserId, EventType.FRIEND, Operation.ADD, toUserId);
        return userDbStorage.findFriends(id)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> deleteFriend(long fromUserId, long toUserId) throws InternalServerException {
        Long from = getUserById(fromUserId).getId();
        Long to = getUserById(toUserId).getId();
        Long id = friendsRequestDbStorage.delete(fromUserId, toUserId);
        eventDbStorage.addEvent(fromUserId, EventType.FRIEND, Operation.REMOVE, toUserId);
        return userDbStorage.findFriends(id)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public Optional<User> find(Long id) {
        return userDbStorage.findById(id);
    }
}
