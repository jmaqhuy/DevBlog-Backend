package com.example.devblogbackend.exception;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler{
    @Value("${spring.application.version}")
    private String API_VERSION;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, WebRequest request) {
        return buildErrorResponse("UnhandledException", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
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
    public ResponseEntity<ApiResponse<Void>> handleCustomException(BusinessException ex) {
        return buildErrorResponse(ex.getErrorType(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return buildErrorResponse("AuthenticationError", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenValidationException(TokenValidationException ex) {
        return buildErrorResponse("TokenValidationError", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return buildErrorResponse("DataIntegrityError", "Database constraint violation", HttpStatus.CONFLICT);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(String errorType, String message, HttpStatus status) {
        return buildErrorResponse(errorType, message, status, null);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            String errorType, String message, HttpStatus status, Map<String, String> details) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .error(new ErrorDetails(errorType, message, details))
                .meta(new Meta(API_VERSION))
                .build();
        return ResponseEntity.status(status).body(response);
    }

}
