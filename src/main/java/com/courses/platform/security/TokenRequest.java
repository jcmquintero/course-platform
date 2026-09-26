package com.courses.platform.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TokenRequest(
        @NotBlank String username,
        @NotNull UserRole role
) {
}