package com.will.cloud.storage.dto;

import lombok.Builder;

@Builder
public record ApiErrorDto(String title, String message, int status, String uri) {}
