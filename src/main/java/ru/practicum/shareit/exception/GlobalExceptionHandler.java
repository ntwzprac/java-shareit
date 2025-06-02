package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.shareit.booking.exceptions.BookingAccessDeniedException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingStatusException;
import ru.practicum.shareit.booking.exceptions.ItemUnavailableException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.request.exceptions.NotFoundException;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.EmailNotGivenException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailNotGivenException.class)
    public ResponseEntity<Map<String, String>> handleEmailNotGivenException(EmailNotGivenException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleItemNotFoundException(ItemNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ItemAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleItemAccessDeniedException(ItemAccessDeniedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ItemUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleItemUnavailableException(ItemUnavailableException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleBookingAccessDeniedException(BookingAccessDeniedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookingNotFoundException(BookingNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BookingStatusException.class)
    public ResponseEntity<Map<String, String>> handleBookingStatusException(BookingStatusException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleItemRequestNotFoundException(NotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("Ошибка: ", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
