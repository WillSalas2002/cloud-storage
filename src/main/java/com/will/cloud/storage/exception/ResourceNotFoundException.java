package com.will.cloud.storage.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourcePath;

    public ResourceNotFoundException(String resourcePath, String message) {
        super(message);
        this.resourcePath = resourcePath;
    }
}
