package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.MPADto;
import ru.yandex.practicum.filmorate.dto.NewMPARequest;
import ru.yandex.practicum.filmorate.model.MPA;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MPAMapper {
    public static MPA mapToMPA(NewMPARequest request) {
        MPA mpa = new MPA();
        mpa.setId(request.getId());
        mpa.setName(request.getName());

        return mpa;
    }

    public static MPADto mapToMPADto(MPA mpa) {
        MPADto dto = new MPADto();
        dto.setId(mpa.getId());
        dto.setName(mpa.getName());

        return dto;
    }
}
