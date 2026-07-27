package com.chatpass.platform.api;

import com.chatpass.platform.workflow.engine.WorkflowValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> security(SecurityException ex) {
        return Map.of("error", "UNAUTHORIZED", "message", ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, WorkflowValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(RuntimeException ex) {
        return Map.of("error", "BAD_REQUEST", "message", ex.getMessage());
    }
}
