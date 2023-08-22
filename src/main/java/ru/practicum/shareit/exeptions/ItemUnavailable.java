package ru.practicum.shareit.exeptions;

public class ItemUnavailable extends RuntimeException {
    public ItemUnavailable(String message) {
        super(message);
    }
}
