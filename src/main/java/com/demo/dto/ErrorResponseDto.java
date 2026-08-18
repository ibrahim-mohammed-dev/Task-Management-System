package com.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) // 👈 عشان الحقول الـ null ما تنزلش في الـ JSON
public class ErrorResponseDto {
    private int status;
    private String message;
    private Map<String, String> errors; // 👈 لدعم أخطاء الـ Validation
    private LocalDateTime timestamp;

    // Constructor للأخطاء العادية (403, 404, 500, إلخ)
    public ErrorResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor مخصص لأخطاء الـ Validation (@Valid)
    public ErrorResponseDto(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, String> getErrors() { return errors; }
    public LocalDateTime getTimestamp() { return timestamp; }
}