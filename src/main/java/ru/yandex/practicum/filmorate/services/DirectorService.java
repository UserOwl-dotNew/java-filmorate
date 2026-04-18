package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.db.DirectorDbStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;

    public List<DirectorDto> getDirectors() {
        return directorStorage.findAll()
                .stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getDirectorById(Long id) {
        return directorStorage.findById(id)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден с ID: " + id));
    }

    public DirectorDto createDirector(NewDirectorRequest request) throws InternalServerException {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ConditionsNotMetException("Имя должно быть указано");
        }

        Optional<Director> alreadyExist = directorStorage.findById(request.getId());
        if (alreadyExist.isPresent()) {
            throw new DuplicatedDataException("Такой режиссер уже существует");
        }

        Director director = DirectorMapper.mapToDirector(request);
        director = directorStorage.create(director);

        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto updateDirector(UpdateDirectorRequest request) {
        Director updateDirector = directorStorage.findById(request.getId())
                .map(director -> DirectorMapper.updateDirectorFields(director, request))
                .orElseThrow(() -> new NotFoundException("Директор не найден"));

        return DirectorMapper.mapToDirectorDto(updateDirector);
    }

    public DirectorDto deleteDirector(Long id) {
        directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id " + id + " не найден"));

        return DirectorMapper.mapToDirectorDto(directorStorage.delete(id));
    }
}
