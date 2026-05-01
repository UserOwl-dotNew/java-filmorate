package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String INSERT_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM directors WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Director create(Director director) throws InternalServerException {
        Long id = insert(
                INSERT_QUERY,
                director.getName()
        );
        director.setId(id);

        return director;
    }

    public Director update(Director director) throws InternalServerException {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public Director delete(Long id) {
        Director deleteDirector = findById(id)
                .orElseThrow(() -> new NotFoundException("Режисёр с id: " + id + " не найден"));
        if (delete(DELETE_BY_ID_QUERY, id)) {
            return deleteDirector;
        }
        return null;
    }

    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
