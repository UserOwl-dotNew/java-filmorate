package ru.yandex.practicum.filmorate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    DirectorDto mapToDirectorDto(Director director);

    Director mapToDirector(NewDirectorRequest request);

    @Mapping(target = "id", ignore = true)
    void updateDirectorFields(@MappingTarget Director director, UpdateDirectorRequest request);
}