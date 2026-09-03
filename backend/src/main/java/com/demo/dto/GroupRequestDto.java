package com.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequestDto(
        @NotBlank(message = "name can't be null")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 chars")
        String name
) {
}