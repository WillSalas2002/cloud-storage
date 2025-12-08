package com.will.cloud.storage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AuthRequest", description = "AuthRequest for signing in and singing up")
public record AuthRequest(
        @NotBlank(message = "Username should be provided.")
                @Size(
                        min = 3,
                        max = 20,
                        message = "Username should be in the range of 3 and 20 chars.")
                String username,
        @NotBlank(message = "Password should not be null or empty.") String password) {}
