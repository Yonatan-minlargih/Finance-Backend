package com.financial.corefinance.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class CoreFinanceExceptionHandler {

    @ApiResponse(responseCode = "400", description = "Bad Request", 
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Request validation failed")
            .details(errors)
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.warn("Bind error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Binding Failed")
            .message("Request binding failed")
            .details(errors)
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .message("Request constraint validation failed")
            .details(errors)
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ApiResponse(responseCode = "404", description = "Not Found")
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Entity Not Found")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Illegal Argument")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Illegal State")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .message("You don't have permission to access this resource")
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ApiResponse(responseCode = "409", description = "Conflict")
    @ExceptionHandler(JournalPostingException.class)
    public ResponseEntity<ErrorResponse> handleJournalPostingException(JournalPostingException ex) {
        log.error("Journal posting error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Journal Posting Failed")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ApiResponse(responseCode = "409", description = "Conflict")
    @ExceptionHandler(AccountValidationException.class)
    public ResponseEntity<ErrorResponse> handleAccountValidationException(AccountValidationException ex) {
        log.error("Account validation error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Account Validation Failed")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ApiResponse(responseCode = "409", description = "Conflict")
    @ExceptionHandler(BudgetValidationException.class)
    public ResponseEntity<ErrorResponse> handleBudgetValidationException(BudgetValidationException ex) {
        log.error("Budget validation error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Budget Validation Failed")
            .message(ex.getMessage())
            .path(getCurrentPath(ex))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please try again later.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getCurrentPath(Exception ex) {
        // This would need to be implemented to extract the request path
        // For now, returning a placeholder
        return "/api/v1/*";
    }
}
