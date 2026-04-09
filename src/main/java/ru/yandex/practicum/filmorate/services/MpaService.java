package ru.yandex.practicum.filmorate.services;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MPADto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MPAMapper;
import ru.yandex.practicum.filmorate.storage.db.MPADbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MpaService {
    private final MPADbStorage mpaStorage;

    public MpaService(MPADbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<MPADto> getMpa() {
        return mpaStorage.findAll()
                .stream()
                .map(MPAMapper::mapToMPADto)
                .collect(Collectors.toList());
    }

    public MPADto getMpaById(long id) {
        return mpaStorage.findById(id)
                .map(MPAMapper::mapToMPADto)
                .orElseThrow(() -> new NotFoundException("MPA не найден с ID: " + id));
    }
}
