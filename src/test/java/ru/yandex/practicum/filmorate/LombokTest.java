package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.User;

public class LombokTest {
    public static void main(String[] args) {
        User user = new User();
        user.setId(1L);  // Должно работать
        System.out.println(user.getId());  // Должно работать
    }
}