package com.rakesh.taskmanagement.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rakesh.taskmanagement.dto.ValidationErrorResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto(e.getMessage(), 404));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error occurred - {} validation failures detected", ex.getBindingResult().getFieldErrorCount());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach( error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
            log.debug("Field validation failed: {} - {}", error.getField(), error.getDefaultMessage());
        });

        ValidationErrorResponse response = new ValidationErrorResponse(
                "Validation failed", fieldErrors
        );
        
        log.info("Returning validation error response with {} field errors", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation occurred - {} violations detected", ex.getConstraintViolations().size());
        
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String error = violation.getPropertyPath() + ": " + violation.getMessage();
            errors.add(error);
            log.debug("Constraint violation: {}", error);
        }
        
        String errorMessage = String.join(", ", errors);
        log.info("Returning constraint violation response: {}", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDto(errorMessage, 400));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Invalid data provided";
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("not-null constraint")) {
                message = "Required field is missing";
            } else if (ex.getMessage().contains("unique constraint")) {
                message = "Duplicate value not allowed";
            }
        }
        log.error("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDto(message, 400));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        log.error("Unhandled exception occurred: {}", e.getClass().getSimpleName(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("An unexpected error occurred", 500));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.warn("JSON parsing error occurred: Invalid data format received");
        log.debug("JSON parse error details: {}", ex.getMessage());
        
        String message = "Invalid data format";

        if(ex.getMessage().contains("Cannot deserialize value of type")) {
            if(ex.getMessage().contains("TaskStatus")) {
                message = "Invalid task status. Valid values: TODO, IN_PROGRESS, DONE";
                log.info("Invalid TaskStatus value provided in request");
            } else if (ex.getMessage().contains("Priority")) {
                message = "Invalid priority. Valid values: LOW, MEDIUM, HIGH";
                log.info("Invalid Priority value provided in request");
            }
        }

        log.info("Returning JSON parse error response: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDto(message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: User attempted unauthorized action - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponseDto("Access denied: You don't have permission to perform this action", 403));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument provided: {}", ex.getMessage());
        log.debug("IllegalArgumentException details", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDto("Invalid argument: " + ex.getMessage(), 400));
    }
}
