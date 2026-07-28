package com.golfclub.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//Catches errors from every controller so
//we do not need try/catch in each one.
//Anything not listed here is left to Spring.
@RestControllerAdvice
public class GlobalExceptionHandler {

    //The shape of our error JSON.
    public record ApiError(
            LocalDateTime timestamp,
            int status,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
    }

    //Errors we threw ourselves.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest request) {
        ApiError error = new ApiError(LocalDateTime.now(), ex.getStatus().value(),
                ex.getMessage(), request.getRequestURI(), null);
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    //@Valid failed. Send back every bad field.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiError error = new ApiError(LocalDateTime.now(), 400,
                "One or more fields are invalid", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    //A parameter was the wrong type,
    //for example ?type=PLATINUM
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleBadParam(MethodArgumentTypeMismatchException ex,
                                                   HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() + "'";
        ApiError error = new ApiError(LocalDateTime.now(), 400, message,
                request.getRequestURI(), null);
        return ResponseEntity.badRequest().body(error);
    }
}
