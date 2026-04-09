package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Optional;

@Repository
public class MPADbStorage extends BaseDbStorage<MPA> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa";
    private static final String INSERT_QUERY = "INSERT INTO mpa(name) VALUES (?)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE id = ?";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM mpa WHERE id = ?";

    public MPADbStorage(JdbcTemplate jdbc, RowMapper<MPA> mapper) {
        super(jdbc, mapper);
    }

    public List<MPA> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public MPA create(MPA mpa) throws InternalServerException {
        Long id = insert(
            INSERT_QUERY,
                mpa.getName()
        );
        mpa.setId(id);

        return mpa;
    }

    public MPA delete(Long id) {
        Optional<MPA> deleteMPA = findById(id);
        if (delete(DELETE_BY_ID_QUERY, id)) {
            return deleteMPA.get();
        }
        return null;
    }

    public Optional<MPA> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}
