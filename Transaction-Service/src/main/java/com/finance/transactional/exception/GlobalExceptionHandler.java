package com.finance.transactional.exception;

import feign.FeignException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(DuplicateVendorException.class)
    public ResponseEntity<java.util.Map<String, String>> handleDuplicateVendorException(DuplicateVendorException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                java.util.Map.of(
                        "error", DuplicateVendorException.MESSAGE,
                        "message", DuplicateVendorException.MESSAGE));
    }

    @ExceptionHandler(DuplicateInvoiceException.class)
    public ResponseEntity<java.util.Map<String, String>> handleDuplicateInvoiceException(DuplicateInvoiceException e) {
        String message = e.getMessage() != null ? e.getMessage() : DuplicateInvoiceException.MESSAGE;
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                java.util.Map.of("error", message, "message", message));
    }

    @ExceptionHandler(ApInvoiceApprovalException.class)
    public ResponseEntity<java.util.Map<String, String>> handleApInvoiceApprovalException(ApInvoiceApprovalException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of(
                        "error", e.getMessage(),
                        "message", e.getMessage()));
    }

    @ExceptionHandler(ClosedAccountingPeriodException.class)
    public ResponseEntity<java.util.Map<String, String>> handleClosedAccountingPeriodException(
            ClosedAccountingPeriodException e) {
        String message = e.getMessage() != null ? e.getMessage() : ClosedAccountingPeriodException.MESSAGE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of("error", message, "message", message));
    }

    @ExceptionHandler(PurchaseOrderVendorMismatchException.class)
    public ResponseEntity<java.util.Map<String, String>> handlePurchaseOrderVendorMismatchException(
            PurchaseOrderVendorMismatchException e) {
        String message = e.getMessage() != null ? e.getMessage() : PurchaseOrderVendorMismatchException.MESSAGE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of("error", message, "message", message));
    }

    @ExceptionHandler(PurchaseOrderInvoicedAmountExceededException.class)
    public ResponseEntity<java.util.Map<String, String>> handlePurchaseOrderInvoicedAmountExceededException(
            PurchaseOrderInvoicedAmountExceededException e) {
        String message = e.getMessage() != null ? e.getMessage() : PurchaseOrderInvoicedAmountExceededException.MESSAGE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of("error", message, "message", message));
    }

    @ExceptionHandler(PurchaseOrderPaymentExceededException.class)
    public ResponseEntity<java.util.Map<String, String>> handlePurchaseOrderPaymentExceededException(
            PurchaseOrderPaymentExceededException e) {
        String message = e.getMessage() != null ? e.getMessage() : PurchaseOrderPaymentExceededException.MESSAGE;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of("error", message, "message", message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(FeignException.Forbidden.class)
    public ResponseEntity<String> handleFeignForbiddenException(FeignException.Forbidden e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied - " + e.getMessage());
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> handleFeignNotFoundException(FeignException.NotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<String> handleFeignBadRequestException(FeignException.BadRequest e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<String> handleFeignConflictException(FeignException.Conflict e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    public ResponseEntity<String> handleFeignServiceUnavailableException(FeignException.ServiceUnavailable e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    public ResponseEntity<String> handleFeignInternalServerErrorException(FeignException.InternalServerError e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<List<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errorMessages);
    }
}
