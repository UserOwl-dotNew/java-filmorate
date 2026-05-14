package ru.yandex.practicum.filmorate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "eventType", expression = "java(event.getEventType().name())")
    @Mapping(target = "operation", expression = "java(event.getOperation().name())")
    EventDto mapToEventDto(Event event);
}