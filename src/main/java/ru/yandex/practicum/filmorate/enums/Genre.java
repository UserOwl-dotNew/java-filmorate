//package ru.yandex.practicum.filmorate.enums;
//
//import lombok.Getter;
//@Getter
//public enum Genre {
//    КОМЕДИЯ(1, "Комедия"),
//    ДРАМА(2, "Драма"),
//    МУЛЬТФИЛЬМ(3, "Мультфильм"),
//    ТРИЛЛЕР(4, "Триллер"),
//    ДОКУМЕНТАЛЬНЫЙ(5, "Документальный"),
//    БОЕВИК(6, "Боевик");
//
//    @Getter
//    private final int id;
//    @Getter
//    private final String name;
//
//    Genre(int id, String name) {
//        this.id = id;
//        this.name = name;
//    }
//
//    Genre() {}
//
//    public static Genre fromId(int id) {
//        for (Genre genre : values()) {
//            if(genre.getId() == id) {
//                return genre;
//            }
//        }
//        return null;
//    }
//
//    public static int fromGenre(Genre genre) {
//        return genre.getId();
//    }
//}
