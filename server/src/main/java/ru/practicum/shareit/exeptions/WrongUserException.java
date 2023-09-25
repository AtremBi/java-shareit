package ru.practicum.shareit.exeptions;

public class WrongUserException extends RuntimeException {
    public WrongUserException(String message) {
        super(message);
    }
}
