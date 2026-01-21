package com.rakesh.taskmanagement.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private String message;
    private String timestamp;
    private int status;

    public ErrorResponseDto(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
        this.status = 400;
    }

    public ErrorResponseDto(String message, int status) {
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
    }
}
