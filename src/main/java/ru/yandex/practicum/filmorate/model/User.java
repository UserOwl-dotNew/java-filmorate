package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;


/*
 * User.
 */
@Data
@EqualsAndHashCode(of = {"email"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    private String name;
    @NotNull
    private LocalDate birthday;
}
