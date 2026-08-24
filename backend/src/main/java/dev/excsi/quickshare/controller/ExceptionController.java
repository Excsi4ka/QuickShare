package dev.excsi.quickshare.controller;

import dev.excsi.quickshare.dto.ErrorResponse;
import dev.excsi.quickshare.exception.BadInputException;
import dev.excsi.quickshare.exception.UserAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> errorAlreadyRegistered(UserAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("USER_ALREADY_EXISTS", String.format("Email: %s already associated with an account", exception.getMessage())));
    }

    @ExceptionHandler(BadInputException.class)
    public ResponseEntity<ErrorResponse> badInputRequest(BadInputException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", exception.getMessage()));
    }
}
