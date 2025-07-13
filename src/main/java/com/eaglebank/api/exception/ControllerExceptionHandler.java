package com.eaglebank.api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.MissingResourceException;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(MissingResourceException.class)
    public ResponseEntity<Object> handleMissingResources(MissingResourceException ex, WebRequest req) {
        return new ResponseEntity<>(Map.of("error", "Not Found", "message", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        return new ResponseEntity<>(Map.of("error", "Forbidden", "message", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex, WebRequest req) {
        return new ResponseEntity<>(Map.of("error", "Bad Request", "message", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFunds(InsufficientFundsException ex) {
        return new ResponseEntity<>(Map.of("message", ex.getMessage()), HttpStatus.UNPROCESSABLE_ENTITY); // 422
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // Catches user deletion conflicts
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(Map.of("message", "Cannot delete resource. It is still referenced by other entities."), HttpStatus.CONFLICT); // 409
    }
}
