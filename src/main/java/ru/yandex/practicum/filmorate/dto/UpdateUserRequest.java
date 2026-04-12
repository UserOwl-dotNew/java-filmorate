package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private Long id;
    private String email;
    private String name;
    private String login;
    private LocalDate birthday;

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasLogin() {
        return !(login == null || login.isBlank());
    }

    public boolean hasBirthday() {
        return !(birthday == null);
    }
}
