package com.officedubac.project.exceptions;

import org.springframework.http.HttpStatus;

public class BadCredentialsExceptionException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BadCredentialsExceptionException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
