package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.services.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<DirectorDto> findAll() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DirectorDto findById(@PathVariable("id") Long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto create(@RequestBody NewDirectorRequest request) throws InternalServerException {
        return directorService.createDirector(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public DirectorDto update(@RequestBody UpdateDirectorRequest request) {
        return directorService.updateDirector(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public DirectorDto delete(@PathVariable("id") Long id) {
        return directorService.deleteDirector(id);
    }
}
