package com.rakesh.taskmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidParameter(InvalidParameterException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDto(e.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto(e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("An unexpected error occurred"));
    }
}
