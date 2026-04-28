package com.officedubac.project.exception;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends GlobalHandlerControllerException {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> handleDuplicateKey(DuplicateKeyException ex) {
        return new ResponseEntity<>("Erreur : Clé dupliquée en base de données", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MongoException.class)
    public ResponseEntity<String> handleMongoException(MongoException ex) {
        return new ResponseEntity<>("Erreur MongoDB : " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

