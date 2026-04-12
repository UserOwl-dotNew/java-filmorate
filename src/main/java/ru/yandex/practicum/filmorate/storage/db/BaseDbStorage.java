package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected void update(String query, Object... params) throws InternalServerException {
        int rowsUpdate = jdbc.update(query, params);
        if (rowsUpdate == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    public boolean delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }

    protected long insert(String query, Object... params) throws InternalServerException {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = null;


        id = keyHolder.getKeyAs(Long.class);

        if (id == null) {
            try {
                Integer intId = keyHolder.getKeyAs(Integer.class);
                if (intId != null) {
                    id = intId.longValue();
                }
            } catch (Exception e) {
                // Игнорируем
            }
        }
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected long count(String query, Object... params) {
        return jdbc.queryForObject(query, Long.class, params);
    }
}
