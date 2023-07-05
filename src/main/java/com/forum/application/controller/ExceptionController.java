package com.forum.application.controller;

import com.forum.application.dto.ResponseMessage;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleNotFoundException(ResourceNotFoundException ex) {
        var responseMessage = new ResponseMessage(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoLoggedInUserException.class)
    public ResponseEntity<ResponseMessage> handleNoLoggedInUserException(NoLoggedInUserException ex) {
        var responseMessage = new ResponseMessage(HttpStatus.FORBIDDEN, ex.getMessage());
        return new ResponseEntity<>(responseMessage, HttpStatus.FORBIDDEN);
    }
}
