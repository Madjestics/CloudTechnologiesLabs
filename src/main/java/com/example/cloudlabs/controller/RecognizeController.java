package com.example.cloudlabs.controller;

import com.example.cloudlabs.dto.RecognizeCarLabelsResponse;
import com.example.cloudlabs.service.RecognizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Controller
@RequestMapping("/recognize")
@RequiredArgsConstructor
public class RecognizeController {
    private final RecognizeService recognizeService;

    @GetMapping
    public String showForm() {
        return "recognize/cars";
    }

    @PostMapping
    public String recognize(@RequestParam("file") MultipartFile file,
                                            Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран");
            return "recognize/cars";
        }

        try {
            RecognizeCarLabelsResponse response = recognizeService.recognizeCarLabels(file);
            model.addAttribute("response", response);
            model.addAttribute("uploadedFileName", file.getOriginalFilename());
            try (InputStream inputStream = file.getInputStream()) {
                String dataUri = recognizeService.createImage(inputStream, response);
                model.addAttribute("imageDataUrl", dataUri);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при вызове API: " + e.getMessage());
        }
        return "recognize/cars";
    }

}
