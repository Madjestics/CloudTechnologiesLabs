package com.example.cloudlabs.controller;

import com.example.cloudlabs.dto.DirectorDto;
import com.example.cloudlabs.entity.Director;
import com.example.cloudlabs.mapper.DirectorMapper;
import com.example.cloudlabs.service.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/director")
public class DirectorController {
    private final DirectorService directorService;
    private final DirectorMapper directorMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DirectorDto> findAll() {
        return directorService.findAll().stream()
                .map(directorMapper::map).toList();
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public DirectorDto findById(@PathVariable(name = "id") Long id) {
        return directorMapper.map(directorService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto addDirector(@RequestBody DirectorDto dto) {
        Director saved = directorService.add(directorMapper.map(dto));
        return directorMapper.map(saved);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public DirectorDto updateDirector(@RequestBody DirectorDto dto, @PathVariable(name = "id") Long id) {
        Director updated = directorService.update(directorMapper.map(dto), id);
        return directorMapper.map(updated);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDirector(@PathVariable(name = "id") Long id) {
        directorService.delete(id);
    }
}
