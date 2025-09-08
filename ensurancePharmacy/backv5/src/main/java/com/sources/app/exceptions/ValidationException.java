package com.sources.app.exceptions;

/**
 * Exception thrown when input validation fails.
 * This provides more specific error handling than generic Exception.
 */
public class ValidationException extends Exception {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
