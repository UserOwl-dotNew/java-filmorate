package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet st, int rowNum) throws SQLException {
        User user = new User();
        user.setId(st.getLong("id"));
        user.setEmail(st.getString("email"));
        user.setName(st.getString("username"));
        user.setLogin(st.getString("login"));

        Date birthday = st.getDate("birthday");
        user.setBirthday(birthday.toLocalDate());

        return user;
    }
}
