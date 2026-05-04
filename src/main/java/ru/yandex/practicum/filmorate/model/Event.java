package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Long timestamp;
}
