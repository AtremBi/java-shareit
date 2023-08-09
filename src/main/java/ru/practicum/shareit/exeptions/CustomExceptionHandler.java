package ru.practicum.shareit.exeptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError handle(NotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("NOT FOUND")
                .status(404)
                .exception("ru.practicum.shareit.exceptions.NotFoundException")
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler({AlreadyExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseError handleAlreadyExists(AlreadyExistException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("CONFLICT")
                .status(409)
                .exception("ru.practicum.shareit.exceptions.AlreadyExistedException")
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError handle(WrongUserException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error("NOT FOUND")
                .status(404)
                .exception("ru.practicum.shareit.exceptions.OwnerNotChangeException")
                .message(exception.getMessage())
                .build();
    }
}
