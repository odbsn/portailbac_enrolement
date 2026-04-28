package com.officedubac.project.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@ToString
public class BusinessResourceException extends RuntimeException {

    private Long resourceId;
    private String errorCode;
    private HttpStatus status;

    public BusinessResourceException(String message) {
        super(message);
    }

    public BusinessResourceException(Long resourceId, String message) {
        super(message);
        this.resourceId = resourceId;
    }

    public BusinessResourceException(Long resourceId, String errorCode, String message) {
        super(message);
        this.resourceId = resourceId;
        this.errorCode = errorCode;
    }

    public BusinessResourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessResourceException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    public BusinessResourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
