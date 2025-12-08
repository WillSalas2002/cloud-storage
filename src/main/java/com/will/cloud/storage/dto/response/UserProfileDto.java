package com.will.cloud.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfileDto", description = "UserProfileDto returns user's profile")
public record UserProfileDto(String username) {}
