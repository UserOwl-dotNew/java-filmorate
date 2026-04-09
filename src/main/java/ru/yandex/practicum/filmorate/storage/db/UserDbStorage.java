package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Qualifier("userDbStorage")
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String INSERT_QUERY = "INSERT INTO users(login, email, username, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET login = ?, email = ?, username = ?, birthday = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_FRIENDS_BY_ID_QUERY = "SELECT *\n" +
            "FROM users\n" +
            "WHERE id IN (\n" +
            "    SELECT to_user_id\n" +
            "    FROM friend_request\n" +
            "    WHERE status_id = 1\n" +
            "    AND from_user_id = ?\n" +
            ");";
    private static final String FIND_MUTUAL_FRIENDS_QUERY = "SELECT *\n" +
            "FROM users\n" +
            "WHERE id IN (SELECT fr.to_user_id\n" +
            "FROM friend_request fr\n" +
            "WHERE fr.from_user_id = ? \n" +
            "  AND fr.status_id = 1\n" +
            "  AND fr.to_user_id IN (\n" +
            "    SELECT to_user_id \n" +
            "    FROM friend_request \n" +
            "    WHERE from_user_id = ? AND status_id = 1\n" +
            "  ));";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User create(User user) throws InternalServerException {
        Long id = insert(
                INSERT_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday()
        );

        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) throws InternalServerException {
        update(
                UPDATE_QUERY,
                newUser.getLogin(),
                newUser.getEmail(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public User delete(Long id) throws InternalServerException {
        Optional<User> deleteUser = findById(id);
        if (delete(DELETE_BY_ID_QUERY, id)) {
            return deleteUser.get();
        }
        return null;
    }

    public Optional<User> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public List<User> findFriends(long id) {
        return findMany(FIND_FRIENDS_BY_ID_QUERY, id);
    }

    public List<User> findMutualFriends(long fromUserId, long toUserId) {
        return findMany(FIND_MUTUAL_FRIENDS_QUERY, fromUserId, toUserId);
    }
}
