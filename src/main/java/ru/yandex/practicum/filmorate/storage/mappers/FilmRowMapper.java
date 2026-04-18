package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
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
            film.setMpa(mpa);
        }
        Long directorId = rs.getLong("director_id");
        if (directorId > 0 && !rs.wasNull()) {
            Director director = new Director();
            director.setId(directorId);
            film.setDirectors(director);
        }

        return film;
    }
}
