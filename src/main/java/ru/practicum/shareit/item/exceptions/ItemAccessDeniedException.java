package ru.practicum.shareit.item.exceptions;

public class ItemAccessDeniedException extends RuntimeException {
    public ItemAccessDeniedException(String message) {
        super(message);
    }
} 