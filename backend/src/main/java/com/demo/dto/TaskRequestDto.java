package com.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequestDto
        (
    @NotBlank(message = "can't the title be null")
     String title,
     String description
        ){}