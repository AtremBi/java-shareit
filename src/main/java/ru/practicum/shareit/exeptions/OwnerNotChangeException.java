package ru.practicum.shareit.exeptions;

public class OwnerNotChangeException extends RuntimeException{
    public OwnerNotChangeException(String message){
        super(message);
    }
}
