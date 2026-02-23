package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


/*
 * User.
 */
@Getter
@Setter
@Data
public class User {
    Long id;
    @Email
    String email;
    @NotNull
    @NotBlank
    String login;
    String name;
    @Past
    @NotNull
    LocalDate birthday;
}
