package com.demo.dto;

import com.demo.model.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequestDto(
        @NotNull(message = "Role cannot be empty")
        Role role
) {
}
