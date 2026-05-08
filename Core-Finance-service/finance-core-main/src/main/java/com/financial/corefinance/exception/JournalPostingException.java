package com.financial.corefinance.exception;

public class JournalPostingException extends RuntimeException {
    
    public JournalPostingException(String message) {
        super(message);
    }
    
    public JournalPostingException(String message, Throwable cause) {
        super(message, cause);
    }
}
