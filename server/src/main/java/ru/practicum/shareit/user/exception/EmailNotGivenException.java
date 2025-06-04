package ru.practicum.shareit.user.exception;

public class EmailNotGivenException extends RuntimeException {
    public EmailNotGivenException(String message) {
        super(message);
    }
}
