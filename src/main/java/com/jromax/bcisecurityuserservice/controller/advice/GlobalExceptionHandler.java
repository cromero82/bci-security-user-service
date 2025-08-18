package com.jromax.bcisecurityuserservice.controller.advice;

import com.jromax.bcisecurityuserservice.model.dto.error.ErrorDTO;
import com.jromax.bcisecurityuserservice.model.dto.error.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        // Collect unique (field, message) pairs to avoid duplicate entries when multiple constraints fail on the same field
        List<ErrorDTO> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + "|" + errorMessage;
                })
                .distinct()
                .map(key -> {
                    String[] parts = key.split("\\|", 2);
                    String fieldName = parts[0];
                    String errorMessage = parts.length > 1 ? parts[1] : "Invalid value";
                    log.debug("Validation error on field '{}': {}", fieldName, errorMessage);
                    return ErrorDTO.builder()
                            .timestamp(LocalDateTime.now())
                            .codigo(HttpStatus.BAD_REQUEST.value())
                            .detail(fieldName + ": " + errorMessage)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Returning {} validation errors with status {}", errors.size(), HttpStatus.BAD_REQUEST);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder().error(errors).build());
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityExistsException(EntityExistsException ex) {
        log.error("Entity already exists: {}", ex.getMessage());
        
        ErrorDTO error = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .codigo(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .build();

        log.info("Returning conflict error with status {}", HttpStatus.CONFLICT);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponseDTO.builder().error(Collections.singletonList(error)).build());
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        
        ErrorDTO error = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .codigo(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .build();

        log.info("Returning not found error with status {}", HttpStatus.NOT_FOUND);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.builder().error(Collections.singletonList(error)).build());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        
        ErrorDTO error = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .codigo(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .build();

        log.info("Returning bad request error with status {}", HttpStatus.BAD_REQUEST);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder().error(Collections.singletonList(error)).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorDTO error = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .codigo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred: " + ex.getMessage())
                .build();

        log.info("Returning internal server error with status {}", HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder().error(Collections.singletonList(error)).build());
    }
}