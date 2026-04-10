package com.chatpass.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    
    private final HttpStatus status = HttpStatus.NOT_FOUND;
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s with id %d not found", resource, id));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
}