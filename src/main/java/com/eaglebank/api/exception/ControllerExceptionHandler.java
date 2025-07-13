package com.eaglebank.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
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
}
