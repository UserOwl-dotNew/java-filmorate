package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.SortFilms;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ParameterNotValidException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
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
            "ORDER BY f.release_date DESC;";
    private static final String SELECT_GENRES_QUERY = "SELECT g.* FROM genre g " +
            "JOIN film_genre fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String SELECT_DIRECTORS_QUERY = "SELECT d.* FROM directors d " +
            "JOIN film_directors fd ON d.id = fd.director_id " +
            "WHERE fd.film_id = ?";
    private static final String SELECT_MPA_QUERY = "SELECT * FROM mpa WHERE id = ?";
    private static final String SELECT_DIRECTOR_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String DELETE_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";

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
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            List<Object[]> batchArgs = new ArrayList<>();
            for (Long directorId : uniqueDirectorIds) {
                batchArgs.add(new Object[]{newFilm.getId(), directorId});
            }

            jdbc.batchUpdate(INSERT_DIRECTOR_QUERY, batchArgs);
        }

        loadGenres(newFilm);
        loadDirector(newFilm);
        loadMPA(newFilm);

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

    public List<Film> findFilmsByDirector(Long directorId, String sortBy) {
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
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("director_name"));
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
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
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
}
