package ru.yandex.practicum.filmorate.controller;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.services.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(GenreController.class);
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<GenreDto> findAll() {
        return genreService.getGenres();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GenreDto getGenresById(@PathVariable("id") Long id) {
        log.info("Requested genre id: {}", id);
        GenreDto result = genreService.getGenreById(id);
        log.info("Found genre: {}", result);
        return result;
    }
}
