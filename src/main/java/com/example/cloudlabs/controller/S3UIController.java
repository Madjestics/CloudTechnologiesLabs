package com.example.cloudlabs.controller;

import com.example.cloudlabs.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3UIController {
    private final S3Service s3Service;

    @GetMapping
    public String index(Model model, @ModelAttribute("message") String message) {
        List<String> buckets = s3Service.listBuckets();
        model.addAttribute("buckets", buckets);
        if (message != null && !message.isEmpty()) model.addAttribute("message", message);
        return "s3/index";
    }

    @PostMapping("/createBucket")
    public String createBucket(@RequestParam("bucketName") String bucketName, RedirectAttributes ra) {
        try {
            s3Service.createBucket(bucketName);
            ra.addFlashAttribute("message", "Бакет успешно создан: " + bucketName);
        } catch (Exception e) {
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/s3";
    }

    @PostMapping("/deleteBucket")
    public String deleteBucket(@RequestParam("bucketName") String bucketName, RedirectAttributes ra) {
        try {
            s3Service.removeBucket(bucketName);
            ra.addFlashAttribute("message", "Бакет успешно удален: " + bucketName);
        } catch (Exception e) {
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/s3";
    }

    @GetMapping("/buckets/{bucket}")
    public String viewBucket(@PathVariable String bucket, Model model, @ModelAttribute("message") String message) {
        try {
            List<String> objects = s3Service.listObjects(bucket);
            model.addAttribute("objects", objects);
            model.addAttribute("bucket", bucket);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        if (message != null && !message.isEmpty()) model.addAttribute("message", message);
        return "s3/bucket";
    }

    @PostMapping("/buckets/{bucket}/delete") public String deleteObjectUi(@PathVariable String bucket,
                                                                          @RequestParam("name") String objectName,
                                                                          RedirectAttributes ra) {
        try {
            s3Service.removeObject(bucket, objectName);
            ra.addFlashAttribute("message", "Объект успешно удален: " + objectName);
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Ошибка удаления объекта: " + e.getMessage());
        }
        return "redirect:/s3/buckets/" + UriUtils.encodePathSegment(bucket, StandardCharsets.UTF_8); }

    @PostMapping("/buckets/{bucket}/upload")
    public String uploadFile(@PathVariable String bucket,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes ra) {
        try {
            s3Service.uploadFile(bucket, file);
            ra.addFlashAttribute("message", "Файл успешно загружен: " + file.getOriginalFilename());
        } catch (Exception e) {
            ra.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/s3/buckets/" + bucket;
    }
}
