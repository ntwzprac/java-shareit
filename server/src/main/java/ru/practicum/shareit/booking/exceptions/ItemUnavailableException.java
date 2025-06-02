package ru.practicum.shareit.booking.exceptions;

public class ItemUnavailableException extends RuntimeException {
    public ItemUnavailableException(String message) {
        super(message);
    }
}