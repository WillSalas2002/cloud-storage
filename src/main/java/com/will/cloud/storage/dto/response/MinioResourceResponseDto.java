package com.will.cloud.storage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MinioResourceResponse", description = "MinioResourceResponse - minio response")
public record MinioResourceResponseDto(
        String path,
        String name,
        @JsonInclude(value = JsonInclude.Include.NON_DEFAULT) Long size,
        ResourceType type) {}
