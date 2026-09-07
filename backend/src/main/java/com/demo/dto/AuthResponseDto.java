package com.demo.dto;

public class AuthResponseDto {
    private String token;
    private String refreshToken;
    private String type = "Bearer";

    public AuthResponseDto(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}