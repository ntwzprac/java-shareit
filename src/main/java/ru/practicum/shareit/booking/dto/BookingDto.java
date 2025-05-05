package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
public class BookingDto {
    private LocalDateTime end;
    private ItemDto item;
    private BookingStatus status;

    public static BookingDto toBookingDto(ru.practicum.shareit.booking.model.Booking booking) {
        return new BookingDto(
                booking.getEnd(),
                ItemDto.toItemDto(booking.getItem()),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return new Booking(null, null, bookingDto.getEnd(), ItemDto.toItem(bookingDto.getItem()), null, bookingDto.getStatus());
    }
}
