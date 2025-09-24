package com.example.cloudlabs.controller;

import com.example.cloudlabs.dto.MovieDto;
import com.example.cloudlabs.dto.StreamSegment;
import com.example.cloudlabs.entity.Movie;
import com.example.cloudlabs.mapper.MovieMapper;
import com.example.cloudlabs.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/movie")
public class MovieController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MovieDto> findAll() {
        return movieService.findAll()
                .stream().map(movieMapper::map).toList();
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public MovieDto findById(@PathVariable(name = "id") Long id) {
        return movieMapper.map(movieService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDto addMovie(@RequestBody MovieDto dto) {
        Movie saved = movieService.add(movieMapper.map(dto));
        return movieMapper.map(saved);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public MovieDto updateMovie(@RequestBody MovieDto dto, @PathVariable Long id) {
        Movie updated = movieService.update(movieMapper.map(dto), id);
        return movieMapper.map(updated);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
    }

    @PostMapping("upload/{id}")
    public void uploadMovie(@RequestPart("movie") MultipartFile multipartFile, @PathVariable Long id) {
        movieService.uploadMovie(multipartFile, id);
    }

    @GetMapping(value = "/watch/{id}")
    public ResponseEntity<StreamingResponseBody> watchMovie(@PathVariable Long id,
                                                            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        StreamSegment seg = movieService.prepareStreamSegment(id, rangeHeader);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.setContentType(MediaType.parseMediaType(seg.getContentType()));

        StreamingResponseBody body = outputStream -> {
            // Обязательно закрываем InputStream — try-with-resources
            try (InputStream in = seg.openStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    try {
                        outputStream.write(buffer, 0, read);
                        outputStream.flush();
                    } catch (IOException ioEx) {
                        if (isClientAbort(ioEx)) {
                            log.info("Client aborted download for movieId={} (start={} end={}). " +
                                            "Это нормальное поведение при кликах/переключениях.",
                                    id, seg.getStart(), seg.getEnd());
                        } else {
                            log.warn("IOException while streaming movieId={}, start={}, end={}: {}",
                                    id, seg.getStart(), seg.getEnd(), ioEx.getMessage());
                        }
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        log.debug("Streaming thread interrupted for movieId={}", id);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error streaming movieId=" + id, e);
            }
        };

        if (seg.isPartial()) {
            headers.setContentLength(seg.getContentLength());
            headers.add(HttpHeaders.CONTENT_RANGE,
                    String.format("bytes %d-%d/%d", seg.getStart(), seg.getEnd(), seg.getTotal()));
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).body(body);
        } else {
            headers.setContentLength(seg.getTotal());
            return ResponseEntity.ok().headers(headers).body(body);
        }
    }

    private boolean isClientAbort(IOException ex) {
        if (ex instanceof org.apache.catalina.connector.ClientAbortException) {
            return true;
        }
        String msg = ex.getMessage();
        return msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset by peer"));
    }
}
