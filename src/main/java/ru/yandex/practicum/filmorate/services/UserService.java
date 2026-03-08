package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendsIsExists;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    public final InMemoryUserStorage inMemoryUserStorage;

    public List<User> findAllFriends(Long id) throws NotFoundException {
        return inMemoryUserStorage.findAll()
                .stream()
                .filter(user -> inMemoryUserStorage.findById(id).get().getFriends().contains(user.getId()))
                .toList();
    }

    public List<User> findMutualFriends(Long friendIdTo, Long friendIdFrom) {
        Set<Long> friendsUserTo = inMemoryUserStorage.findById(friendIdTo).get().getFriends();
        Set<Long> friendsUserFrom = inMemoryUserStorage.findById(friendIdFrom).get().getFriends();

        return friendsUserTo.stream()
                .filter(friendsUserFrom::contains)
                .map(id -> inMemoryUserStorage.findById(id).get())
                .collect(Collectors.toList());
    }

    public Set<Long> addFriend(Long friendIdTo, Long friendIdFrom) throws NotFoundException {
        Set<Long> friendsUserTo = inMemoryUserStorage.findById(friendIdTo).get().getFriends();
        Set<Long> friendsUserFrom = inMemoryUserStorage.findById(friendIdFrom).get().getFriends();

        if (friendsUserTo.contains(friendIdFrom)) {
            throw new FriendsIsExists("Этот человек уже находится в списке друзей.");
        }
        friendsUserTo.add(friendIdFrom);
        friendsUserFrom.add(friendIdTo);

        return friendsUserTo;
    }

    public Set<Long> deleteFriend(Long friendIdTo, Long friendIdFrom) throws NotFoundException {
        Set<Long> friendsUserTo = inMemoryUserStorage.findById(friendIdTo).get().getFriends();
        Set<Long> friendsUserFrom = inMemoryUserStorage.findById(friendIdFrom).get().getFriends();

        if (!friendsUserTo.contains(friendIdFrom)) {
            throw new FriendsIsExists("Этого человека нет в списке друзей.");
        }

        friendsUserTo.remove(friendIdFrom);
        friendsUserFrom.remove(friendIdTo);

        return friendsUserTo;
    }
}
