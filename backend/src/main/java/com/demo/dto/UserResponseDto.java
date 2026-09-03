package com.demo.dto;

public record UserResponseDto(
        Long id,
        String email,
        String username,
        String groupName
) {
}
