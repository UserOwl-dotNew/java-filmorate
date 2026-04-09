//package ru.yandex.practicum.filmorate.enums;
//
//import lombok.Getter;
//
//@Getter
//public enum MPA {
//    G(1, "G"),
//    PG(2, "PG"),
//    PG_13(3, "PG-13"),
//    R(4, "R"),
//    NC_17(5, "NC-17");
//
//    @Getter
//    private final int id;
//    @Getter
//    private final String g;
//
//    MPA(int id, String g) {
//        this.id = id;
//        this.g = g;
//    }
//
//    public static MPA fromId(int id) {
//        for (MPA mpa : values()) {
//            if(mpa.getId() == id) {
//                return mpa;
//            }
//        }
//        return null;
//    }
//
//    public static int fromMpa(MPA mpa) {
//        return mpa.getId();
//    }
//}
