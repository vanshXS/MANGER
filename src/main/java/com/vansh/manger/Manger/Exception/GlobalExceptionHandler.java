package com.vansh.manger.Manger.Exception;

import com.vansh.manger.Manger.DTO.ErrorResponse;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /* -------------------------------------------------
       1. Validation Errors (Field-level for forms)
       ------------------------------------------------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /* -------------------------------------------------
       2. Illegal Argument (Bad input / ID / params)
       ------------------------------------------------- */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /* -------------------------------------------------
       3. Username Not Found
       ------------------------------------------------- */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /* -------------------------------------------------
       4. IO Exceptions (File upload, streams, etc.)
       ------------------------------------------------- */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /* -------------------------------------------------
       5. Illegal State (Business rule violations)
       ------------------------------------------------- */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex
    ) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /* -------------------------------------------------
       6. JDBC / Database Exceptions
       ------------------------------------------------- */
    @ExceptionHandler(JDBCException.class)
    public ResponseEntity<ErrorResponse> handleJDBCException(JDBCException ex) {
        return buildErrorResponse("Database error occurred", HttpStatus.BAD_REQUEST);
    }

    /* -------------------------------------------------
       7. Runtime Exception (Catch-all fallback)
       ------------------------------------------------- */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /* -------------------------------------------------
       Helper Method
       ------------------------------------------------- */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String message,
            HttpStatus status
    ) {
        ErrorResponse response = new ErrorResponse(
                message,
                status.value(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(response);
    }
}
