package com.demo.dto;

public record TokensResponseDto(
        String token,
        String refreshToken
) {
}
