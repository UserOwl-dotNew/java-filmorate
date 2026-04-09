package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.FriendRequest;

import java.util.List;

@Repository
public class FriendsRequestDbStorage extends BaseDbStorage<FriendRequest> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM friend_request";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friend_request (from_user_id, to_user_id, status_id)" +
            "VALUES (?, ?, 1)";
    private static final String FIND_FRIENDS_BY_ID_QUERY = "SELECT *\n" +
            "FROM users\n" +
            "WHERE id = ? IN (\n" +
            "    SELECT from_user_id\n" +
            "    FROM friend_request\n" +
            "    WHERE status_id = 1\n" +
            ");";
    private static final String UPDATE_QUERY = "UPDATE friend_request SET status = ? WHERE from_user_id = ? AND to_user_id = ?;";
    private static final String DELETE_QUERY = "DELETE FROM friend_request WHERE from_user_id = ? AND to_user_id = ?";

    public FriendsRequestDbStorage(JdbcTemplate jdbc, RowMapper<FriendRequest> mapper) {
        super(jdbc, mapper);
    }

    public List<FriendRequest> findAll() {
        return super.findMany(FIND_ALL_QUERY);
    }

    public Long create(long fromUserId, long toUserId) throws InternalServerException {
        if (fromUserId == toUserId) {
            throw new ConditionsNotMetException("Нельзя добавить самого себя в друзья");
        }

        super.insert(
                ADD_FRIEND_QUERY,
                fromUserId,
                toUserId
        );

        return fromUserId;
    }

    public Long delete(long fromUserId, long toUserId) {
        delete(DELETE_QUERY, fromUserId, toUserId);
        return toUserId;
    }
}
