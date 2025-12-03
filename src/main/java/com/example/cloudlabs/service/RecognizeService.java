package com.example.cloudlabs.service;

import com.example.cloudlabs.dto.RecognizeCarLabelsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface RecognizeService {
    RecognizeCarLabelsResponse recognizeCarLabels(MultipartFile multipartFile) throws IOException;

    String createImage(InputStream imageStream, RecognizeCarLabelsResponse response) throws IOException;
}
