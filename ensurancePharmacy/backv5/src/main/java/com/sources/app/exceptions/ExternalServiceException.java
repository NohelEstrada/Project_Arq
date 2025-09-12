package com.sources.app.exceptions;

/**
 * Exception thrown when external service calls fail.
 * This provides more specific error handling than generic Exception.
 */
public class ExternalServiceException extends Exception {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExternalServiceException(Throwable cause) {
        super(cause);
    }
}
