package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.List;

@Qualifier
@Repository
public class LikeDbStorage extends BaseDbStorage<Like> {
    private FilmDbStorage filmDbStorage;
    private static final String INSERT_QUERY = "INSERT INTO likes(user_id, film_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM likes WHERE film_id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM likes WHERE film_id = ?";

    public LikeDbStorage(JdbcTemplate jdbc, RowMapper<Like> mapper) {
        super(jdbc, mapper);
    }

    public boolean create(Long userId, Long filmId) throws InternalServerException {
        List<Like> likesForFilm = findByFilmId(filmId);
        boolean likeExists = likesForFilm.stream()
                .anyMatch(like -> like.getUserId().equals(userId));
        if (likeExists) {
            return false;
        }

        insert(INSERT_QUERY, userId, filmId);
        return true;
    }

    public boolean delete(Long userId, Long filmId) {
        int rowsDelete = jdbc.update(DELETE_BY_ID_QUERY, userId, filmId);
        return rowsDelete > 0;
    }

    public List<Like> findByFilmId(Long id) {
        return findMany(FIND_BY_FILM_ID_QUERY, id);
    }

    public Long countLikes(Long filmId) {
        return count(COUNT_QUERY, filmId);
    }
}
