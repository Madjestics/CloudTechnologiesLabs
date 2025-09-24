package com.example.cloudlabs.service.impl;

import com.example.cloudlabs.dto.Action;
import com.example.cloudlabs.dto.MovieEvent;
import com.example.cloudlabs.dto.StreamSegment;
import com.example.cloudlabs.entity.Movie;
import com.example.cloudlabs.exception.EntityNotFoundException;
import com.example.cloudlabs.exception.ValidationException;
import com.example.cloudlabs.mapper.MovieMapper;
import com.example.cloudlabs.repository.DirectorRepository;
import com.example.cloudlabs.repository.MovieRepository;
import com.example.cloudlabs.service.MessageService;
import com.example.cloudlabs.service.MovieService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private static final long MAX_CHUNK = 5 * 1024 * 1024L; // 5 MB

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final MessageService messageService;
    private final MinioClient minioClient;
    private final MovieMapper movieMapper;

    @Value("${storage.bucket}")
    private String moviesBucket;

    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Movie findById(Long id) {
        if (id == null) {
            throw new ValidationException("ID не может быть пустым");
        }
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Фильм не найден"));
    }

    public Movie add(Movie entity) {
        validate(entity);
        Movie savedMovie = movieRepository.save(entity);
        messageService.sendMovieEvent(new MovieEvent(Action.ADD, movieMapper.map(savedMovie)));
        return savedMovie;
    }

    public Movie update(Movie entity, Long id) {
        validate(entity);
        if (!id.equals(entity.getId())) {
            throw new ValidationException("Переданы разные id");
        }
        Movie existedMovie = movieRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Фильм для обновления не найден"));
        entity.setFilePath(existedMovie.getFilePath());
        Movie updated = movieRepository.save(entity);
        messageService.sendMovieEvent(new MovieEvent(Action.UPDATE, movieMapper.map(updated)));
        return updated;
    }
    
    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException("Недопустимый ID");
        }
        Movie movie = movieRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Фильм для удаления не найден"));
        movieRepository.deleteById(id);
        messageService.sendMovieEvent(new MovieEvent(Action.DELETE, movieMapper.map(movie)));
    }


    public void uploadMovie(MultipartFile multipartFile, Long id) {
        try {
            // имя файла (можно добавить UUID, чтобы избежать коллизий)
            String objectName = id + "/" + multipartFile.getOriginalFilename();

            // загружаем в MinIO
            try (InputStream is = multipartFile.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(moviesBucket)
                                .object(objectName)
                                .stream(is, multipartFile.getSize(), -1)
                                .contentType(multipartFile.getContentType())
                                .build()
                );
            }

            // обновляем запись о фильме
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Не найден фильм с id:" + id));
            movie.setFilePath(objectName);
            movieRepository.save(movie);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    public StreamSegment prepareStreamSegment(Long movieId, String rangeHeader) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found: " + movieId));

        String objectName = movie.getFilePath();

        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(moviesBucket)
                            .object(objectName)
                            .build()
            );

            long totalSize = stat.size();
            String contentType = stat.contentType() != null ? stat.contentType() : "application/octet-stream";

            if (!StringUtils.hasText(rangeHeader)) {
                long start = 0;
                long end = totalSize - 1;
                Supplier<InputStream> supplier = getInputStreamSupplier(end, start, objectName);
                return new StreamSegment(start, end, totalSize, contentType, false, supplier);
            }

            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.isEmpty()) {
                throw new IllegalArgumentException("Invalid Range header: " + rangeHeader);
            }

            HttpRange requested = ranges.get(0);
            long start = requested.getRangeStart(totalSize);
            long end = requested.getRangeEnd(totalSize);

            if (start >= totalSize) {
                throw new IllegalArgumentException("Range start >= total size");
            }
            long maxAllowedEnd = Math.min(totalSize - 1, start + MAX_CHUNK - 1);
            if (end > maxAllowedEnd) {
                end = maxAllowedEnd;
            }

            Supplier<InputStream> supplier = getInputStreamSupplier(end, start, objectName);

            return new StreamSegment(start, end, totalSize, contentType, true, supplier);

        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare stream segment for object: " + objectName, e);
        }
    }

    @NotNull
    private Supplier<InputStream> getInputStreamSupplier(long end, long start, String objectName) {
        final long contentLength = end - start + 1;
        return () -> {
            try {
                return minioClient.getObject(GetObjectArgs.builder()
                        .bucket(moviesBucket)
                        .object(objectName)
                        .offset(start)
                        .length(contentLength)
                        .build());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void validate(Movie movie) {
        if (movie == null) {
            throw new ValidationException("Фильм не может быть null");
        }
        validateLocalFields(movie);
        validateDirector(movie.getDirectorId());
    }

    private void validateLocalFields(Movie movie) {
        if (!StringUtils.hasText(movie.getTitle())) {
            throw new ValidationException("Названия фильма должно быть не пустым");
        }
        if (movie.getTitle().length() > 100) {
            throw new ValidationException("Название фильма должно иметь длину менее 100 символов");
        }
        if (movie.getYear() == null || movie.getYear() < 1900 || movie.getYear() > 2100) {
            throw new ValidationException("Год выхода фильма должен быть между 1900 и 2100");
        }
        if (movie.getDuration() == null || movie.getDuration().isBefore(LocalTime.MIN)) {
            throw new ValidationException("Длительность фильма должна быть больше 0 секунд");
        }
        if (movie.getRating() == null || movie.getRating() < 0 || movie.getRating() > 10) {
            throw new ValidationException("Рейтинг фильма должен быть между 0 и 10 баллами");
        }
        if (!StringUtils.hasText(movie.getGenre())) {
            throw new ValidationException("Жанр фильма не может быть пустым");
        }
    }

    private void validateDirector(Long directorId) {
        if (directorId == null) {
            throw new ValidationException("Режиссера фильма не может быть пустым");
        }
        if (!directorRepository.existsById(directorId)) {
            throw new EntityNotFoundException("Директор фильма не найден");
        }
    }
}
