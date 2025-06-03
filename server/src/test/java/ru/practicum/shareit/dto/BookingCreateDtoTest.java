package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingCreateDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void serializeBookingCreateDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, start, end);

        String json = objectMapper.writeValueAsString(bookingCreateDto);

        assertNotNull(json);
        assertTrue(json.contains("\"itemId\":1"));
        assertTrue(json.contains("\"start\""));
        assertTrue(json.contains("\"end\""));
    }

    @Test
    void deserializeBookingCreateDto() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2024-03-20T10:00:00\",\"end\":\"2024-03-20T11:00:00\"}";

        BookingCreateDto bookingCreateDto = objectMapper.readValue(json, BookingCreateDto.class);

        assertNotNull(bookingCreateDto);
        assertEquals(1L, bookingCreateDto.getItemId());
        assertEquals(LocalDateTime.parse("2024-03-20T10:00:00"), bookingCreateDto.getStart());
        assertEquals(LocalDateTime.parse("2024-03-20T11:00:00"), bookingCreateDto.getEnd());
    }
}