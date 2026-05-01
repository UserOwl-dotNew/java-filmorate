package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Qualifier
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE_BY_ID_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String FIND_POPULAR_QUERY = "SELECT f.*,\n" +
            "\t\tm.id AS mpa_id,\n" +
            "\t\tm.name AS mpa_name,\n" +
            "\t\tSTRING_AGG(g.id, ',') AS genre_id,\n" +
            "\t\tSTRING_AGG(g.name, ',') AS genre_name\n" +
            "FROM films f\n" +
            "LEFT JOIN mpa m ON f.mpa_id = m.id\n" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id\n" +
            "LEFT JOIN genre g ON fg.genre_id = g.id\n" +
            "LEFT JOIN likes l ON f.id = l.film_id\n" +
            "GROUP BY f.id, m.id, m.name\n" +
            "ORDER BY COUNT(l.id) DESC\n" +
            "LIMIT ?;";
    private static final String SELECT_GENRES_QUERY = "SELECT g.* FROM genre g " +
            "JOIN film_genre fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String SELECT_MPA_QUERY = "SELECT * FROM mpa WHERE id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    // Поиск только по названию
    private static final String SEARCH_BY_TITLE_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))";
    // Поиск только по описанию
    public static final String SEARCH_BY_DESCRIPTION_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id" +
                    "WHERE LOWER(f.description) LIKE LOWER(CONCAT('%', ?, '%'))";
    // Поиск по названию или описанию
    public static final String SEARCH_BY_TITLE_AND_DESCRIPTION_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "   OR LOWER(f.description) LIKE LOWER(CONCAT('%', ?, '%'))";

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film create(Film film) throws InternalServerException {
        Long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Object[]> batchArgs = new ArrayList<>();
            for (Long genreId : uniqueGenreIds) {
                batchArgs.add(new Object[]{film.getId(), genreId});
            }
            jdbc.batchUpdate(INSERT_GENRE_QUERY, batchArgs);
        }
        loadGenres(film);
        loadMPA(film);

        return film;
    }

    @Override
    public Film delete(Long id) {
        Optional<Film> deleteFilm = findById(id);
        if (deleteFilm.isPresent()) {
            delete(DELETE_BY_ID_QUERY, id);
            return deleteFilm.get();
        }
        return null;
    }

    public Optional<Film> findById(long id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, id);
        if (filmOptional.isPresent()) {
            loadGenres(filmOptional.get());
            loadMPA(filmOptional.get());
            return filmOptional;
        }
        return Optional.empty();
    }

    @Override
    public Film update(Film newFilm) throws InternalServerException {
        update(
                UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                newFilm.getId()
        );

        jdbc.update(DELETE_GENRES_QUERY, newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = newFilm.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Object[]> batchArgs = new ArrayList<>();
            for (Long genreId : uniqueGenreIds) {
                batchArgs.add(new Object[]{newFilm.getId(), genreId});
            }

            jdbc.batchUpdate(INSERT_GENRE_QUERY, batchArgs);
        }

        loadGenres(newFilm);

        return newFilm;
    }

    public List<Film> findPopular(Number count) {
        return jdbc.query(FIND_POPULAR_QUERY, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getDouble("duration"));

            MPA mpa = new MPA();
            mpa.setId(rs.getLong("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            String genreIds = rs.getString("genre_id");
            String genreNames = rs.getString("genre_name");
            if (genreIds != null && genreNames != null) {
                String[] ids = genreIds.split(",");
                String[] names = genreNames.split(",");
                List<Genre> genres = new ArrayList<>();
                for (int i = 0; i < ids.length; i++) {
                    Genre genre = new Genre();
                    genre.setId(Long.parseLong(ids[i]));
                    genre.setName(names[i]);
                    genres.add(genre);
                }
                film.setGenres(genres);
            } else {
                film.setGenres(new ArrayList<>());
            }
            return film;
        }, count);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbc.query(SELECT_GENRES_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        film.setGenres(genres);
    }

    private void loadMPA(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            return;
        }

        try {
            MPA mpa = jdbc.queryForObject(SELECT_MPA_QUERY, (rs, rowNum) -> {
                MPA result = new MPA();
                result.setId(rs.getLong("id"));
                result.setName(rs.getString("name"));
                return result;
            }, film.getMpa().getId());
            film.setMpa(mpa);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA не найден");
        }
    }

    public List<Film> search(String query, List<String> by) {
        if (query == null || query.isBlank() || by == null || by.isEmpty()) {
            return List.of();
        }

        boolean byTitle = by.contains("title");
        boolean byDescription = by.contains("description");

        List<Film> films;

        if (byTitle && byDescription) {
            films = jdbc.query(
                    SEARCH_BY_TITLE_AND_DESCRIPTION_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query, query  // два ? в SQL — передаём query дважды
            );
        } else if (byTitle) {
            films = jdbc.query(
                    SEARCH_BY_TITLE_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query
            );
        } else if (byDescription) {
            films = jdbc.query(
                    SEARCH_BY_DESCRIPTION_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query
            );
        } else {
            return List.of();
        }
        films.forEach(film -> {
            loadGenres(film);
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                loadMPA(film);
            }
        });
        return films;
    }

    private Film mapFilmFromRs(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getDouble("duration"));

        Long mpaId = rs.getLong("mpa_id");
        if (mpaId > 0 && !rs.wasNull()) {
            MPA mpa = new MPA();
            mpa.setId(mpaId);
            try {
                mpa.setName(rs.getString("mpa_name"));
            } catch (SQLException iqnored) {
            }

            film.setMpa(mpa);
        }
        return film;
    }
}
