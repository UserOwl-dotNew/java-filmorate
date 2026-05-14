package ru.yandex.practicum.filmorate.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.storage.db.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventDbStorage eventDbStorage;
    private final UserDbStorage userDbStorage;
    private final EventMapper eventMapper;  // ← Добавили MapStruct mapper

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        eventDbStorage.addEvent(userId, eventType, operation, entityId);
    }

    public List<EventDto> getUserFeed(Long userId) {
        if (userDbStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return eventDbStorage.getUserFeed(userId).stream()
                .map(eventMapper::mapToEventDto)  // ← Используем MapStruct
                .collect(Collectors.toList());
    }
}