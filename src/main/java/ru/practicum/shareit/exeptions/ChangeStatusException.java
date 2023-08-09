package ru.practicum.shareit.exeptions;

public class ChangeStatusException extends RuntimeException{
    public ChangeStatusException(String message){
        super(message);
    }
}
