package com.will.cloud.storage.config.minio;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String url;
    private String username;
    private String password;
    private String bucket;
}
