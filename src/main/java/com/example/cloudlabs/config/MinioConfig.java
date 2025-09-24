package com.example.cloudlabs.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class MinioConfig {

    @Value("${storage.endpoint:localhost:9091}")
    private String endpoint;

    @Value("${storage.accessKey}")
    private String accessKey;

    @Value("${storage.secretKey}")
    private String secretKey;

    @Value("${storage.region:us-east-1}")
    private String region;

    @Value("${storage.bucket}")
    private String moviesBucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .region(region)
                .build();
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(moviesBucket).build())) {
                client.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(moviesBucket)
                                .build()
                );
            }
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return client;
    }
}
