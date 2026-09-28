package com.courses.platform.enrollment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateProgressRequest(
        @Min(0)
        @Max(100)
        int progress) {
}