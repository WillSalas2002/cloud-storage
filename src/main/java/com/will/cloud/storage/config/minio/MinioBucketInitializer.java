package com.will.cloud.storage.config.minio;

import static com.will.cloud.storage.util.AppConstants.BUCKET_NAME;

import com.will.cloud.storage.service.MinioUtils;

import jakarta.annotation.PostConstruct;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MinioBucketInitializer {
    private final MinioUtils minioUtils;

    @PostConstruct
    public void init() {
        minioUtils.createBucket(BUCKET_NAME);
    }
}
