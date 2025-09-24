package com.example.cloudlabs.service.impl;

import com.example.cloudlabs.entity.Director;
import com.example.cloudlabs.entity.Movie;
import com.example.cloudlabs.exception.EntityNotFoundException;
import com.example.cloudlabs.exception.InternalServerException;
import com.example.cloudlabs.exception.ValidationException;
import com.example.cloudlabs.repository.DirectorRepository;
import com.example.cloudlabs.repository.MovieRepository;
import com.example.cloudlabs.service.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    public List<Director> findAll() {
        return directorRepository.findAll();
    }

    public Director findById(Long id) {
        if (id == null) {
            throw new ValidationException("Недопустимый ID");
        }
        return directorRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Режиссер не найден"));
    }

    public Director add(Director director) {
        validate(director);
        return directorRepository.save(director);
    }

    public Director update(Director director, Long id) {
        if (id == null || !id.equals(director.getId())) {
            throw new ValidationException("Недопустимый ID");
        }
        if (!directorRepository.existsById(id)) {
            throw new EntityNotFoundException("Режиссер для обновления информации не найден");
        }
        validate(director);
        return directorRepository.save(director);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException("Недопустимый ID");
        }
        if (!directorRepository.existsById(id)) {
            throw new EntityNotFoundException("Режиссер для удаления не найден");
        }
        List<Movie> moviesByDirector = movieRepository.findMoviesByDirectorId(id);
        if (!moviesByDirector.isEmpty()) {
           throw new InternalServerException("У режиссера еще остались неудаленные фильмы");
        }
        directorRepository.deleteById(id);
    }

    private void validate(Director director) {
        if (director == null) {
            throw new ValidationException("Режиссер не может быть null");
        }
        if (!StringUtils.hasText(director.getFio())) {
            throw new ValidationException("ФИО режиссера не может быть пустым");
        }
    }
}
