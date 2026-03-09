package ru.yandex.practicum.filmorate.services;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendsIsExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(UserService.class);
    public final InMemoryUserStorage inMemoryUserStorage;

    public List<User> findAllFriends(Long id) throws NotFoundException {
        return inMemoryUserStorage.findAll()
                .stream()
                .filter(user -> {
                    return InMemoryUserStorage.findById(id)
                            .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + id + " не найден"))
                            .getFriends().contains(user.getId());
                })
                .toList();
    }

    public List<User> findMutualFriends(Long friendIdTo, Long friendIdFrom) {
        Set<Long> friendsUserTo = InMemoryUserStorage.findById(friendIdTo)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdTo + " не найден"))
                .getFriends();
        if (friendsUserTo == null) {
            friendsUserTo = new HashSet<>();
        }

        Set<Long> friendsUserFrom = InMemoryUserStorage.findById(friendIdFrom)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdFrom + " не найден"))
                .getFriends();
        if (friendsUserFrom == null) {
            friendsUserFrom = new HashSet<>();
        }

        return friendsUserTo.stream()
                .filter(friendsUserFrom::contains)
                .map(id -> {
                    return InMemoryUserStorage.findById(id)
                            .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdFrom + " не найден"));
                })
                .collect(Collectors.toList());
    }

    public Set<Long> addFriend(Long friendIdTo, Long friendIdFrom) throws NotFoundException {
        log.info("friendIdTo: {}, friendIdFrom: {}", friendIdTo, friendIdFrom);
        Set<Long> friendsUserTo = InMemoryUserStorage.findById(friendIdTo)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdTo + " не найден"))
                .getFriends();
        if (friendsUserTo == null) {
            friendsUserTo = new HashSet<>();
        }
        log.info("friendsUserTo: {}", friendsUserTo);
        Set<Long> friendsUserFrom = InMemoryUserStorage.findById(friendIdFrom)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdFrom + " не найден"))
                .getFriends();
        if (friendsUserFrom == null) {
            friendsUserFrom = new HashSet<>();
        }
        log.info("friendsUserFrom: {}", friendsUserFrom);

        if (friendsUserTo.contains(friendIdFrom)) {
            throw new FriendsIsExists("Этот человек уже находится в списке друзей.");
        }
        log.info("Пользователи с id: {}, {} добавлены в друзья", friendIdFrom, friendIdTo);
        friendsUserTo.add(friendIdFrom);
        friendsUserFrom.add(friendIdTo);

        return friendsUserTo;
    }

    public Set<Long> deleteFriend(Long friendIdTo, Long friendIdFrom) throws NotFoundException {
        Set<Long> friendsUserTo = InMemoryUserStorage.findById(friendIdTo)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdTo + " не найден"))
                .getFriends();
        if (friendsUserTo == null) {
            friendsUserTo = new HashSet<>();
        }

        Set<Long> friendsUserFrom = InMemoryUserStorage.findById(friendIdFrom)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id: " + friendIdFrom + " не найден"))
                .getFriends();
        if (friendsUserFrom == null) {
            friendsUserFrom = new HashSet<>();
        }

        if (!friendsUserTo.contains(friendIdFrom)) {
            throw new FriendsIsExists("Этого человека нет в списке друзей.");
        }

        friendsUserTo.remove(friendIdFrom);
        friendsUserFrom.remove(friendIdTo);

        return friendsUserTo;
    }
}
