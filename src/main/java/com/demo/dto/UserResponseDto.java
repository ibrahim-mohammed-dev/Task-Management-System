package com.demo.dto;

import com.demo.model.Role;

public record UserResponseDto(
        Long id,
        String email,
        Role role,
        String username
) {
}
