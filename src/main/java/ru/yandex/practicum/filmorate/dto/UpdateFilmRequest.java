package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@Data
public class UpdateFilmRequest {
    private Long id;
    private List<Genre> genres;
    private MPA mpa;
    private String name;
    private String description;

    public boolean hasGenres() {
        return !(genres == null);
    }

    public boolean hasMPA() {
        return !(mpa == null);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public MPA getMpa() {
        return mpa;
    }

    public void setMpa(MPA mpa) {
        this.mpa = mpa;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
