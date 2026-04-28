package com.officedubac.project.exception;

public class FichierNonTrouveException extends RuntimeException {
    public FichierNonTrouveException(String message) {
        super(message);
    }
    public FichierNonTrouveException(String message, Throwable cause) {
        super(message, cause);
    }
}
