package com.example.cloudlabs.controller;

import com.example.cloudlabs.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3RestController {
    private final S3Service s3Service;


    @PostMapping("/buckets/{bucket}")
    public ResponseEntity<?> createBucket(@PathVariable String bucket) {
        s3Service.createBucket(bucket);
        return ResponseEntity.ok("Бакет создан: " + bucket);
    }

    @GetMapping("/buckets")
    public ResponseEntity<?> listBuckets() {
        List<String> buckets = s3Service.listBuckets();
        return ResponseEntity.ok(buckets);
    }

    @DeleteMapping("/buckets/{bucket}")
    public ResponseEntity<?> deleteBucket(@PathVariable String bucket) {
        s3Service.removeBucket(bucket);
        return ResponseEntity.ok("Бакет удален: " + bucket);
    }

    @GetMapping("/buckets/{bucket}/objects")
    public ResponseEntity<?> listObjects(@PathVariable String bucket) {
        List<String> objects = s3Service.listObjects(bucket);
        return ResponseEntity.ok(objects);
    }

    @PostMapping("/buckets/{bucket}/objects")
    public ResponseEntity<?> upload(@PathVariable String bucket,
                                    @RequestParam("file") MultipartFile file) {
        s3Service.uploadFile(bucket, file);
        return ResponseEntity.ok("Файл успешно загружен: " + file.getOriginalFilename());
    }

    @DeleteMapping("/buckets/{bucket}/objects")
    public ResponseEntity<?> delete(@PathVariable String bucket,
                                    @RequestParam("name") String objectName) {
        s3Service.removeObject(bucket, objectName);
        return ResponseEntity.ok("Файл успешно удален: " + objectName);
    }

    @GetMapping("/buckets/{bucket}/object")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String bucket, @RequestParam("name") String objectName) {
        var stat = s3Service.statObject(bucket, objectName);

        StreamingResponseBody stream = outputStream -> {
            try (InputStream input = s3Service.downloadFile(bucket, objectName)) {
                input.transferTo(outputStream);
            }
        };

        ContentDisposition cd = ContentDisposition.builder("attachment")
                .filename(objectName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentLength(stat.size())
                .contentType(MediaType.parseMediaType(stat.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : stat.contentType()))
                .body(stream);
    }
}
