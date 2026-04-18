package ru.yandex.practicum.filmorate.exception;

public class ParameterNotValidException extends RuntimeException {
    public ParameterNotValidException(String message) {
        super(message);
    }
}
