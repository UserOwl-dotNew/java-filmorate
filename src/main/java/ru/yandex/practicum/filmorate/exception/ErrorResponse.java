package ru.yandex.practicum.filmorate.exception;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse {
    @Getter
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }
}
