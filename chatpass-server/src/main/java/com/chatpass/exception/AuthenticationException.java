package com.chatpass.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    
    private final HttpStatus status = HttpStatus.UNAUTHORIZED;
    
    public AuthenticationException(String message) {
        super(message);
    }
}