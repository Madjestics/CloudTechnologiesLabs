package com.example.cloudlabs.service;

import io.minio.StatObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface S3Service {

    void createBucket(String bucketName);

    List<String> listBuckets();

    void removeBucket(String bucketName);

    List<String> listObjects(String bucketName);

    void uploadFile(String bucketName, MultipartFile file);

    void removeObject(String bucketName, String objectName);

    InputStream downloadFile(String bucketName, String objectName);

    StatObjectResponse statObject(String bucketName, String objectName);
}
