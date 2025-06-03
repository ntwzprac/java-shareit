package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailAlreadyUsedException_ShouldReturnConflictStatus() {
        EmailAlreadyUsedException ex = new EmailAlreadyUsedException("Email already used");
        ResponseEntity<Map<String, String>> response = handler.handleEmailAlreadyUsedException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already used", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFoundStatus() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<Map<String, String>> response = handler.handleUserNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleEmailNotGivenException_ShouldReturnBadRequestStatus() {
        EmailNotGivenException ex = new EmailNotGivenException("Email not given");
        ResponseEntity<Map<String, String>> response = handler.handleEmailNotGivenException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email not given", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleItemNotFoundException_ShouldReturnNotFoundStatus() {
        ItemNotFoundException ex = new ItemNotFoundException("Item not found");
        ResponseEntity<Map<String, String>> response = handler.handleItemNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Item not found", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleItemAccessDeniedException_ShouldReturnForbiddenStatus() {
        ItemAccessDeniedException ex = new ItemAccessDeniedException("Access denied");
        ResponseEntity<Map<String, String>> response = handler.handleItemAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleItemUnavailableException_ShouldReturnBadRequestStatus() {
        ItemUnavailableException ex = new ItemUnavailableException("Item unavailable");
        ResponseEntity<Map<String, String>> response = handler.handleItemUnavailableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Item unavailable", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleBookingAccessDeniedException_ShouldReturnForbiddenStatus() {
        BookingAccessDeniedException ex = new BookingAccessDeniedException("Booking access denied");
        ResponseEntity<Map<String, String>> response = handler.handleBookingAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Booking access denied", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleBookingNotFoundException_ShouldReturnNotFoundStatus() {
        BookingNotFoundException ex = new BookingNotFoundException("Booking not found");
        ResponseEntity<Map<String, String>> response = handler.handleBookingNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Booking not found", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleBookingStatusException_ShouldReturnBadRequestStatus() {
        BookingStatusException ex = new BookingStatusException("Invalid booking status");
        ResponseEntity<Map<String, String>> response = handler.handleBookingStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid booking status", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleItemRequestNotFoundException_ShouldReturnNotFoundStatus() {
        NotFoundException ex = new NotFoundException("Request not found");
        ResponseEntity<Map<String, String>> response = handler.handleItemRequestNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Request not found", response.getBody().get("Ошибка: "));
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestStatus() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "default message");

        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("default message", response.getBody().get("error"));
    }
}