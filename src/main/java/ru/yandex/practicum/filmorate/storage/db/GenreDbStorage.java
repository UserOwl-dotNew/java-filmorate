package ru.yandex.practicum.filmorate.storage.db;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(GenreDbStorage.class);
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre";
    private static final String INSERT_QUERY = "INSERT INTO genre(name) VALUES (?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM genre WHERE id = ?";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM genre WHERE film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Genre create(Genre genre) throws InternalServerException {
        Long id = insert(
                INSERT_QUERY,
                genre.getName()
        );
        genre.setId(id);

        return genre;
    }

    public Genre delete(Long id) {
        Optional<Genre> deleteGenre = findById(id);
        if (delete(DELETE_BY_ID_QUERY, id)) {
            return deleteGenre.get();
        }
        return null;
    }

    public Optional<Genre> findById(Long id) {
        log.info("Looking for genre with id: {}", id);
        Optional<Genre> result = findOne(FIND_BY_ID_QUERY, id);
        log.info("Found: {}", result.isPresent());
        return result;
    }

    public List<Genre> findGenresByFilmId(Long filmId) {
        return findMany(FIND_BY_FILM_ID_QUERY, filmId);
    }
}
