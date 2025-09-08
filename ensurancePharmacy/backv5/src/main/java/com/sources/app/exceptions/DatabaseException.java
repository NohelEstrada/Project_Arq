package com.sources.app.exceptions;

/**
 * Exception thrown when database operations fail.
 * This provides more specific error handling than generic Exception.
 */
public class DatabaseException extends Exception {
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
