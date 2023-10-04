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
                .error(exception.getMessage())
                .status(404)
                .exception("ru.practicum.shareit.exceptions.NotFoundException")
                .build();
    }

    @ExceptionHandler({AlreadyExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseError handleAlreadyExists(AlreadyExistException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error(exception.getMessage())
                .status(409)
                .exception("ru.practicum.shareit.exceptions.AlreadyExistedException")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError handle(WrongUserException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error(exception.getMessage())
                .status(404)
                .exception("ru.practicum.shareit.exceptions.OwnerNotChangeException")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handle(ItemUnavailable exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error(exception.getMessage())
                .status(400)
                .exception("ru.practicum.shareit.exceptions.ItemUnavailable")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError handle(ValidationException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error(exception.getMessage())
                .status(500)
                .exception("ru.practicum.shareit.exceptions.ValidationException")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handle(ChangeStatusException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseError.builder()
                .error(exception.getMessage())
                .status(400)
                .exception("ru.practicum.shareit.exceptions.ChangeStatusException")
                .build();
    }
}
