package ru.practicum.shareit.exeptions;

public class ValidationException extends IllegalArgumentException {
    public ValidationException(String message){
        super(message);
    }
}
