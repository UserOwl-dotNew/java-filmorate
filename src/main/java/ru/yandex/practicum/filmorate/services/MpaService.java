package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MPADto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MPAMapper;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaStorage;

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
