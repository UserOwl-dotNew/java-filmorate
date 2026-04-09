package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendRequest;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendRequestMapper implements RowMapper<FriendRequest> {
    @Override
    public FriendRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
        FriendRequest fr = new FriendRequest();
        fr.setId(rs.getLong("id"));
        fr.setStatus(rs.getLong("status_id"));
        fr.setFromUserId(rs.getLong("from_user_id"));
        fr.setToUserId(rs.getLong("to_user_id"));

        return fr;
    }
}
