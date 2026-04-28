package com.officedubac.project.models;

import org.springframework.http.HttpStatus;

public class BusinessResourceException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessResourceException(String code, String message, HttpStatus status) {
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
