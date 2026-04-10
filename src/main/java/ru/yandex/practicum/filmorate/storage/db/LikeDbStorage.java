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
    FilmDbStorage filmDbStorage;
    private static final String INSERT_QUERY = "INSERT INTO likes(user_id, film_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM likes WHERE film_id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM likes WHERE film_id = ?";

    public LikeDbStorage(JdbcTemplate jdbc, RowMapper<Like> mapper) {
        super(jdbc, mapper);
    }

    public Long create(Long userId, Long filmId) throws InternalServerException {
        List<Like> likesForFilm = findByFilmId(filmId);
        boolean likeExists = likesForFilm.stream()
                .anyMatch(like -> like.getUserId().equals(userId));
        if (likeExists) {
            return countLikes(filmId);
        }

        Long id = insert(
                INSERT_QUERY,
                userId,
                filmId
        );
        
        return countLikes(id);
    }

    public Long delete(Long userId, Long filmId) {
        Long likesCount = countLikes(filmId);
        int rowsDelete = jdbc.update(DELETE_BY_ID_QUERY, userId, filmId);
        if (rowsDelete > 0) {
            return countLikes(filmId);
        }
        return likesCount;
    }

    public List<Like> findByFilmId(Long id) {
        return findMany(FIND_BY_FILM_ID_QUERY, id);
    }

    public Long countLikes(Long filmId) {
        return count(COUNT_QUERY, filmId);
    }
}
