package com.will.cloud.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MinioResourceResponse", description = "MinioResourceResponse - minio response")
public record MinioResourceResponseDto(
        String path, String name, Long size, ResourceType resourceType) {}
