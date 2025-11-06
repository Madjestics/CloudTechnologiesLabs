package com.example.cloudlabs.service.impl;

import com.example.cloudlabs.exception.S3Exception;
import com.example.cloudlabs.service.S3Service;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final MinioClient minioClient;

    @Override
    public void createBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException | GeneralSecurityException | IOException exception) {
            throw new S3Exception("Ошибка при создании бакета S3 " + exception.getMessage());
        }
    }

    @Override
    public List<String> listBuckets() {
        List<Bucket> buckets;
        try {
            buckets = minioClient.listBuckets();
        } catch (MinioException | GeneralSecurityException | IOException exception) {
            throw new S3Exception("Ошибка получения списка бакетов " + exception.getMessage());
        }
        return buckets.stream().map(Bucket::name).collect(Collectors.toList());
    }

    @Override
    public void removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (MinioException | GeneralSecurityException | IOException exception) {
            throw new S3Exception("Ошибка удаления бакета с названием " + bucketName + exception.getMessage());
        }
    }

    @Override
    public List<String> listObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build()
        );
        List<String> objects = new ArrayList<>();
        results.forEach(itemResult -> {
            try {
                objects.add(itemResult.get().objectName());
            } catch (MinioException | GeneralSecurityException | IOException exception) {
                throw new S3Exception("Ошибка получения списка объектов из бакета " + bucketName + exception.getMessage());
            }
        });
        return objects;
    }

    @Override
    public void uploadFile(String bucketName, MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            long size = file.getSize();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(file.getOriginalFilename())
                    .stream(is, size, PutObjectArgs.MIN_MULTIPART_SIZE)
                    .contentType(file.getContentType())
                    .build());
        } catch (MinioException | IOException | GeneralSecurityException exception) {
            throw new S3Exception("Ошибка загрузки нового объекта в бакет " + bucketName + exception.getMessage());
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        }  catch (MinioException | IOException | GeneralSecurityException exception) {
            throw new S3Exception("Ошибка удаления объекта " + bucketName + exception.getMessage());
        }
    }

    @Override
    public InputStream  downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (MinioException | IOException | GeneralSecurityException exception) {
            throw new S3Exception("Ошибка получения объекта " + objectName + exception.getMessage());
        }
    }

    @Override
    public StatObjectResponse statObject(String bucketName, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (MinioException | IOException | GeneralSecurityException exception) {
            throw new S3Exception("Ошибка получения стат данных объекта " + objectName + exception.getMessage());
        }
    }
}
