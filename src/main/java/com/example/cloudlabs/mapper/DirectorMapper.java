package com.example.cloudlabs.mapper;

import com.example.cloudlabs.dto.DirectorDto;
import com.example.cloudlabs.entity.Director;
import org.springframework.stereotype.Component;

@Component
public class DirectorMapper {
    public DirectorDto map(Director director) {
        return DirectorDto.builder()
                .id(director.getId())
                .fio(director.getFio())
                .build();
    }

    public Director map(DirectorDto dto) {
        return Director.builder()
                .id(dto.getId())
                .fio(dto.getFio())
                .build();
    }
}
