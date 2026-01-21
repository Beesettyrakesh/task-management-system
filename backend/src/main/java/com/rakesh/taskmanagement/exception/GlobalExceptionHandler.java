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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.servlet.View;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final View error;

    public GlobalExceptionHandler(View error) {
        this.error = error;
    }

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
    
    // Handle @Valid annotation validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach( error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ValidationErrorResponse response = new ValidationErrorResponse(
                "Validation failed", fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }
    
    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        
        String errorMessage = String.join(", ", errors);
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDto(errorMessage));
    }
    
    // Handle database constraint violations (fallback)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Invalid data provided";
        
        // Check for common constraint violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("not-null constraint")) {
                message = "Required field is missing";
            } else if (ex.getMessage().contains("unique constraint")) {
                message = "Duplicate value not allowed";
            }
        }
        
        return ResponseEntity.badRequest()
            .body(new ErrorResponseDto(message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        // Log the actual error for debugging (in production, use proper logging)
        System.err.println("Unhandled exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDto("An unexpected error occurred"));
    }

    public ResponseEntity<ErrorResponseDto> handleJsonParseError(HttpMessageNotReadableException ex) {
        String message = "Invalid data format";

        if(ex.getMessage().contains("Cannot deserialize value of type")) {
            if(ex.getMessage().contains("TaskStatus")) {
                message = "Invalid task status. Valid values: TODO, IN_PROGRESS, DONE";
            } else if (ex.getMessage().contains("Priority")) {
                message = "Invalid priority. Valid values: LOW, MEDIUM, HIGH";
            }
        }

        return ResponseEntity.badRequest().body(new ErrorResponseDto(message));
    }
}
