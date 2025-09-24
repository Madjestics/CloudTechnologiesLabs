package com.example.cloudlabs.mapper;

import com.example.cloudlabs.dto.MovieDto;
import com.example.cloudlabs.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
    public Movie map(MovieDto dto) {
        return new Movie(
                dto.getId(),
                dto.getTitle(),
                dto.getYear(),
                dto.getDuration(),
                dto.getRating(),
                dto.getGenre(),
                dto.getDirectorId()
        );
    }

    public MovieDto map(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getYear(),
                movie.getDuration(),
                movie.getRating(),
                movie.getGenre(),
                movie.getDirectorId()
        );
    }
}
