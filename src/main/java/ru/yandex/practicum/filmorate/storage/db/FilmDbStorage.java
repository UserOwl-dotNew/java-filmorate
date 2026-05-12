package ru.yandex.practicum.filmorate.storage.db;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.enums.SortFilms;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ParameterNotValidException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.services.DirectorService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Qualifier
@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FilmDbStorage.class);
    private static final String FIND_ALL_QUERY = "SELECT * FROM films;";
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
    private static final String FIND_FILMS_BY_DIRECTORS_SORT_BY_LIKES = "SELECT f.*,\n" +
            "\t\td.id director_id,\n" +
            "\t\td.name director_name,\n" +
            "\t\tm.id mpa_id,\n" +
            "\t\tm.name mpa_name,\n" +
            "\t\tSTRING_AGG(g.id, ',') genres_id,\n" +
            "\t\tSTRING_AGG(g.name, ',') genres_name\n" +
            "FROM films f\t\n" +
            "LEFT JOIN film_directors fd ON f.id = fd.film_id\n" +
            "LEFT JOIN directors d ON fd.director_id = d.id\n" +
            "LEFT JOIN likes l ON f.id = l.film_id\n" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id\n" +
            "LEFT JOIN genre g ON fg.genre_id = g.id\n" +
            "LEFT JOIN mpa m ON f.mpa_id = m.id\n" +
            "WHERE d.id = ?\n" +
            "GROUP BY f.id, d.id, d.name, mpa_id, mpa_name\n" +
            "ORDER BY count(l.id) DESC;";
    private static final String FIND_FILMS_BY_DIRECTORS_SORT_BY_YEAR = "SELECT f.*,\n" +
            "\t\td.id director_id,\n" +
            "\t\td.name director_name,\n" +
            "\t\tm.id mpa_id,\n" +
            "\t\tm.name mpa_name,\n" +
            "\t\tSTRING_AGG(g.id, ',') genres_id,\n" +
            "\t\tSTRING_AGG(g.name, ',') genres_name\n" +
            "FROM films f\t\n" +
            "LEFT JOIN film_directors fd ON f.id = fd.film_id\n" +
            "LEFT JOIN directors d ON fd.director_id = d.id\n" +
            "LEFT JOIN likes l ON f.id = l.film_id\n" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id\n" +
            "LEFT JOIN genre g ON fg.genre_id = g.id\n" +
            "LEFT JOIN mpa m ON f.mpa_id = m.id\n" +
            "WHERE d.id = ?\n" +
            "GROUP BY f.id, d.id, d.name, mpa_id, mpa_name\n" +
            "ORDER BY f.release_date ASC;";
    private static final String SELECT_GENRES_QUERY = "SELECT g.* FROM genre g " +
            "JOIN film_genre fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String SELECT_DIRECTORS_QUERY = "SELECT d.* FROM directors d " +
            "JOIN film_directors fd ON d.id = fd.director_id " +
            "WHERE fd.film_id = ?";
    private static final String SELECT_MPA_QUERY = "SELECT * FROM mpa WHERE id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    // Поиск только по названию
    private static final String SEARCH_BY_TITLE_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";
    // Поиск только по описанию
    private static final String SEARCH_BY_DESCRIPTION_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "WHERE LOWER(f.description) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";
    // Поиск по названию или описанию
    private static final String SEARCH_BY_TITLE_AND_DESCRIPTION_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "   OR LOWER(f.description) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";
    private static final String DELETE_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String SEARCH_BY_DIRECTOR_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "JOIN directors d ON fd.director_id = d.id " +
                    "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";

    private static final String SEARCH_BY_TITLE_OR_DIRECTOR_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.id " +
                    "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "   OR LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%')) " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";
    private static final String RECOMMENDATIONS_FILMS_QUERY = "WITH similar_users AS (\n" +
            "    SELECT DISTINCT l2.user_id\n" +
            "    FROM likes l1\n" +
            "    JOIN likes l2 ON l1.film_id = l2.film_id\n" +
            "    WHERE l1.user_id = ? AND l2.user_id != ?\n" +
            "    LIMIT 1\n" +
            ")\n" +
            "SELECT f.*,\n" +
            "\tm.id AS mpa_id,\n" +
            "\tm.name AS mpa_name,\n" +
            "\tSTRING_AGG(g.id, ',') AS genres_id,\n" +
            "\tSTRING_AGG(g.name, ',') AS genres_name\n" +
            "FROM films f\n" +
            "LEFT JOIN mpa m ON f.mpa_id = m.id\n" +
            "LEFT JOIN film_genre fg ON f.id = fg.film_id\n" +
            "LEFT JOIN genre g ON fg.genre_id = g.id\n" +
            "LEFT JOIN likes l ON l.film_id = f.id \n" +
            "WHERE f.id IN (\n" +
            "\tSELECT l.film_id\n" +
            "\tFROM likes l\n" +
            "\tWHERE l.user_id IN (SELECT user_id FROM similar_users)\n" +
            "\t  AND l.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?)\n" +
            "\tGROUP BY l.film_id\n" +
            "\tORDER BY COUNT(DISTINCT l.user_id) DESC, l.film_id\n" +
            ")\n" +
            "GROUP BY f.id, m.id, m.name\n" +
            "ORDER BY count(l.id) DESC;";
    private static final String FIND_COMMON_FILMS_QUERY =
            "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "JOIN likes l1 ON f.id = l1.film_id AND l1.user_id = ? " +
                    "JOIN likes l2 ON f.id = l2.film_id AND l2.user_id = ? " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id, m.id, m.name " +
                    "ORDER BY COUNT(l.id) DESC";

    private static DirectorService directorService;
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, DirectorService directorService) {
        super(jdbc, mapper);
        this.directorService = directorService;
    }

    @Override
    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .peek(film -> {
                    loadGenres(film);
                    loadDirector(film);
                    loadMPA(film);
                })
                .collect(Collectors.toList());
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

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> uniqueDirectorIds = film.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());

            List<Object[]> batchArgs = new ArrayList<>();
            for (Long directorId : uniqueDirectorIds) {
                batchArgs.add(new Object[]{film.getId(), directorId});
            }
            jdbc.batchUpdate(INSERT_DIRECTOR_QUERY, batchArgs);
        }

        loadGenres(film);
        loadMPA(film);
        loadDirector(film);

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
            loadDirector(filmOptional.get());
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

        jdbc.update(DELETE_DIRECTORS_QUERY, newFilm.getId());
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            Set<Long> uniqueDirectorIds = newFilm.getDirectors()
                    .stream()
                    .map(director -> {
                        log.info("director.getId() from newFilm: {}", director.getId());
                        return director.getId();
                    })
                    .collect(Collectors.toSet());
            List<Object[]> batchArgs = new ArrayList<>();
            for (Long directorId : uniqueDirectorIds) {
                batchArgs.add(new Object[]{newFilm.getId(), directorId});
                log.info("batchArgs.add(" + directorId + ")");
            }

            log.info("batchArgs: " + batchArgs.toString());
            jdbc.batchUpdate(INSERT_DIRECTOR_QUERY, batchArgs);
            log.info("batchUpdate complete");
        }

        loadGenres(newFilm);
        loadDirector(newFilm);
        loadMPA(newFilm);

        return newFilm;
    }

    public List<Film> findPopular(Number count, Long genreId, Integer year) {
        List<Object> params = new ArrayList<>();

        String findPopularQuery = "SELECT f.*,\n" +
                "\t\tm.id AS mpa_id,\n" +
                "\t\tm.name AS mpa_name,\n" +
                "\t\tSTRING_AGG(g.id, ',') AS genre_id,\n" +
                "\t\tSTRING_AGG(g.name, ',') AS genre_name\n" +
                "FROM films f\n" +
                "LEFT JOIN mpa m ON f.mpa_id = m.id\n" +
                "LEFT JOIN film_genre fg ON f.id = fg.film_id\n" +
                "LEFT JOIN genre g ON fg.genre_id = g.id\n" +
                "LEFT JOIN likes l ON f.id = l.film_id\n";

        if (genreId != null && year == null) {
            findPopularQuery = findPopularQuery + "WHERE fg.genre_id = ?\n";
            params.add(genreId);
        }

        if (year != null && genreId == null) {
            findPopularQuery = findPopularQuery + "WHERE EXTRACT(YEAR FROM cast(release_date AS date)) = ?\n";
            params.add(year);
        }

        if (year != null && genreId != null) {
            findPopularQuery = findPopularQuery + "WHERE fg.genre_id = ?\n" +
                    "AND EXTRACT(YEAR FROM cast(release_date AS date)) = ?\n";
            params.add(genreId);
            params.add(year);
        }

        params.add(count);

        findPopularQuery = findPopularQuery + "GROUP BY f.id, m.id, m.name\n" +
                "ORDER BY COUNT(l.id) DESC\n" +
                "LIMIT ?;";

        return jdbc.query(findPopularQuery, (rs, rowNum) -> {
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
                List<Genre> genresWithoutDuplicate = new ArrayList<>(new HashSet<>(genres));
                film.setGenres(genresWithoutDuplicate);
            } else {
                film.setGenres(new ArrayList<>());
            }
            return film;
        }, params.toArray());
    }

    public List<Film> findRecommendationsFilms(Long id) {
        return jdbc.query(RECOMMENDATIONS_FILMS_QUERY, (rs, rowNum) -> {
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

            String genreIds = rs.getString("genres_id");
            String genreNames = rs.getString("genres_name");
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
        }, id, id, id);
    }

    public List<Film> findFilmsByDirector(Long directorId, String sortBy) {
        DirectorDto findDirector = directorService.getDirectorById(directorId);
        if (!(SortFilms.from(sortBy) == SortFilms.LIKES ||
                SortFilms.from(sortBy) == SortFilms.YEAR)) {
            throw new ParameterNotValidException("Неизвестный парметр сортировки");
        }

        if (SortFilms.from(sortBy) == SortFilms.LIKES) {
            return jdbc.query(FIND_FILMS_BY_DIRECTORS_SORT_BY_LIKES, (rs, rowNum) -> {
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

                String genreIds = rs.getString("genres_id");
                String genreNames = rs.getString("genres_name");
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

                Director director = new Director();
                director.setId(rs.getLong("director_id"));
                director.setName(rs.getString("director_name"));
                List<Director> directorList = new ArrayList<>();
                directorList.add(director);
                film.setDirectors(directorList);

                return film;
            }, directorId);
        }

        return jdbc.query(FIND_FILMS_BY_DIRECTORS_SORT_BY_YEAR, (rs, rowNum) -> {
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

            String genreIds = rs.getString("genres_id");
            String genreNames = rs.getString("genres_name");
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

            Director director = new Director();
            Long idFromDirector = rs.getLong("director_id");
            log.info("idFromDirector: {}", idFromDirector);
            director.setId(idFromDirector);
            String nameFromDirector = rs.getString("director_name");
            log.info("nameFromDirector: {}", nameFromDirector);
            director.setName(nameFromDirector);
            List<Director> directorList = new ArrayList<>();
            directorList.add(director);
            film.setDirectors(directorList);

            return film;
        }, directorId);
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

    private void loadDirector(Film film) {
        List<Director> directors = jdbc.query(SELECT_DIRECTORS_QUERY, (rs, rowNum) -> {
            Director director = new Director();
            log.info("loadDirector");
            Long id = rs.getLong("id");
            log.info("id: {}", id);
            director.setId(id);
            String name = rs.getString("name");
            log.info("name: {}", name);
            director.setName(name);
            return director;
        }, film.getId());

        film.setDirectors(directors);
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
        boolean byDirector = by.contains("director");

        List<Film> films;

        if (byTitle && byDescription) {
            films = jdbc.query(SEARCH_BY_TITLE_AND_DESCRIPTION_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query, query  // два ? в SQL — передаём query дважды
            );
        } else if (byTitle && byDirector) {
            films = jdbc.query(SEARCH_BY_TITLE_OR_DIRECTOR_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs), query, query);
        } else if (byTitle) {
            films = jdbc.query(SEARCH_BY_TITLE_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query
            );
        } else if (byDescription) {
            films = jdbc.query(SEARCH_BY_DESCRIPTION_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs),
                    query
            );
        } else if (byDirector) {
            films = jdbc.query(SEARCH_BY_DIRECTOR_QUERY,
                    (rs, rowNum) -> mapFilmFromRs(rs), query);
        } else {
            return List.of();
        }

        films.forEach(film -> {
            loadGenres(film);
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                loadMPA(film);
            }
            loadDirector(film);
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
            } catch (SQLException ignored) {
            }

            film.setMpa(mpa);
        }
        return film;
    }

    public List<Film> findCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbc.query(FIND_COMMON_FILMS_QUERY,
                (rs, rowNum) -> mapFilmFromRs(rs), userId, friendId);
        films.forEach(film -> {
            loadGenres(film);
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                loadMPA(film);
            }
            loadDirector(film);
        });
        return films;
    }
}
