package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingCreateDto, Long userId);

    BookingDto approve(Long bookingId, Long userId, boolean approved);

    BookingDto findById(Long bookingId, Long userId);

    List<BookingDto> findAllByBooker(Long userId, BookingStatus state);

    List<BookingDto> findAllByOwner(Long userId, BookingStatus state);

    List<BookingDto> findAllByUser(Long userId, BookingStatus state);
}
