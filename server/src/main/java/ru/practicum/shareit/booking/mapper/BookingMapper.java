package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(ItemMapper.toItem(bookingDto.getItem()));
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }

    public static Booking toBooking(BookingCreateDto bookingCreateDto, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}