package com.example.devblogbackend.exception;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex, WebRequest request) {
        return buildErrorResponse("UnhandledException", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        err -> err.getField(),
                        err -> err.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));

        return buildErrorResponse("ValidationError", "Invalid input data", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetails> handleCustomException(BusinessException ex) {
        return buildErrorResponse(ex.getErrorType(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException ex) {
        return buildErrorResponse("AuthenticationError", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorDetails> handleTokenValidationException(TokenValidationException ex) {
        return buildErrorResponse("TokenValidationError", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return buildErrorResponse("DataIntegrityError", "Database constraint violation", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildErrorResponse(
                "MissingRequestBody",
                "Request body is required and must be valid JSON",
                HttpStatus.BAD_REQUEST
        );
    }


    private ResponseEntity<ErrorDetails> buildErrorResponse(String errorType, String message, HttpStatus status) {
        return buildErrorResponse(errorType, message, status, null);
    }

    private ResponseEntity<ErrorDetails> buildErrorResponse(
            String errorType, String message, HttpStatus status, Map<String, String> details) {
        ErrorDetails error = new ErrorDetails(errorType, message, details);
        return ResponseEntity.status(status).body(error);
    }

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String type;
        private String message;
        private Map<String, String> details;
    }

}
