package com.officedubac.project.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BusinessResourceExceptionDTO {
    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;
    private String requestURL;
    private HttpStatus status;
    private String apiDestintaire;
}

