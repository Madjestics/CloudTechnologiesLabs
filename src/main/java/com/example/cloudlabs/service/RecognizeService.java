package com.example.cloudlabs.service;

import com.example.cloudlabs.dto.RecognizeCarLabelsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface RecognizeService {
    /**
     * Метод для отправки запроса к VK API и получения ответа
     * @param multipartFile - исходное изображение
     * @return - ответ от VK API, преобразованный из формата JSON в объект RecognizeCarLabelsResponse
     * @throws IOException - ошибка обработки изображения
     */
    RecognizeCarLabelsResponse recognizeCarLabels(MultipartFile multipartFile) throws IOException;

    /**
     *
     * @param imageStream - stream данных исходного изображения
     * @param response - ответ от VK API
     * @return строка в формате Base64
     * @throws IOException - ошибка обработки изображения
     */
    String createImage(InputStream imageStream, RecognizeCarLabelsResponse response) throws IOException;
}
