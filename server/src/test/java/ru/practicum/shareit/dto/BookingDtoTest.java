package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void serializeBookingDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        UserDto booker = new UserDto(1L, "Test User", "test@test.com");
        ItemDto item = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);
        BookingDto bookingDto = new BookingDto(1L, start, end, item, booker, BookingStatus.WAITING);

        String json = objectMapper.writeValueAsString(bookingDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"status\":\"WAITING\""));
        assertTrue(json.contains("\"item\""));
        assertTrue(json.contains("\"booker\""));
    }

    @Test
    void deserializeBookingDto() throws Exception {
        String json = "{\"id\":1,\"start\":\"2024-03-20T10:00:00\",\"end\":\"2024-03-20T11:00:00\"," +
                "\"item\":{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true}," +
                "\"booker\":{\"id\":1,\"name\":\"Test User\",\"email\":\"test@test.com\"}," +
                "\"status\":\"WAITING\"}";

        BookingDto bookingDto = objectMapper.readValue(json, BookingDto.class);

        assertNotNull(bookingDto);
        assertEquals(1L, bookingDto.getId());
        assertEquals(BookingStatus.WAITING, bookingDto.getStatus());
        assertNotNull(bookingDto.getItem());
        assertNotNull(bookingDto.getBooker());
    }
}