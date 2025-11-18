package com.will.cloud.storage.dto;

import lombok.Builder;

@Builder
public record ApiErrorDto(
        String title,
        String detail,
        int status,
        String uri
) {}
