package com.will.cloud.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AuthResponse",
        description = "AuthResponse returned for sign in and sing up requests")
public record AuthResponse(String username) {}
