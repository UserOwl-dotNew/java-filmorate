package ru.yandex.practicum.filmorate.enums;

public enum SortFilms {
    YEAR, LIKES;

    public static SortFilms from(String sort) {
        switch (sort.toLowerCase()) {
            case "year":
                return YEAR;
            case "likes":
                return LIKES;
            default:
                return null;
        }
    }
}
