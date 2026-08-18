package com.demo.dto;
public record TaskResponseDto
        (
    Long id,
    String title,
    String description,
    boolean completed
        )
{}