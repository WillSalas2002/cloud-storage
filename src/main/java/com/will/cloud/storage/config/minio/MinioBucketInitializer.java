package com.will.cloud.storage.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

import jakarta.annotation.PostConstruct;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MinioBucketInitializer {
    private final MinioClient client;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() {
        try {
            boolean exists =
                    client.bucketExists(
                            BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());

            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure bucket exists", e);
        }
    }
}
