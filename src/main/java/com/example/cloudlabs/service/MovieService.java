package com.example.cloudlabs.service;

import com.example.cloudlabs.dto.StreamSegment;
import com.example.cloudlabs.entity.Movie;
import org.springframework.web.multipart.MultipartFile;

public interface MovieService extends BaseService<Movie>  {
    void uploadMovie(MultipartFile multipartFile, Long id);
    StreamSegment prepareStreamSegment(Long id, String rangeHeader);
}
