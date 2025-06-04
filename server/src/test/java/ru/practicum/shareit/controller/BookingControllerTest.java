package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.ItemUnavailableException;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingCreateDto bookingCreateDto;
    private BookingDto bookingDto;
    private UserDto userDto;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "Test User", "test@test.com");
        itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        bookingCreateDto = new BookingCreateDto(1L, start, end);

        bookingDto = new BookingDto(1L, start, end, itemDto, userDto, BookingStatus.WAITING);
    }

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(any(BookingCreateDto.class), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));

        verify(bookingService).create(any(BookingCreateDto.class), anyLong());
    }

    @Test
    void createBooking_WithUnavailableItem_ShouldReturnBadRequest() throws Exception {
        when(bookingService.create(any(BookingCreateDto.class), anyLong()))
                .thenThrow(new ItemUnavailableException("Item is not available"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService).create(any(BookingCreateDto.class), anyLong());
    }

    @Test
    void approveBooking_ShouldReturnApprovedBooking() throws Exception {
        BookingDto approvedBooking = new BookingDto(1L, bookingDto.getStart(), bookingDto.getEnd(),
                bookingDto.getItem(), bookingDto.getBooker(), BookingStatus.APPROVED);
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.toString()));

        verify(bookingService).approve(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void approveBooking_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new BookingNotFoundException("Booking not found"));

        mockMvc.perform(patch("/bookings/999")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());

        verify(bookingService).approve(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void findById_ShouldReturnBooking() throws Exception {
        when(bookingService.findById(anyLong(), anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().toString()));

        verify(bookingService).findById(anyLong(), anyLong());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(bookingService.findById(anyLong(), anyLong()))
                .thenThrow(new BookingNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());

        verify(bookingService).findById(anyLong(), anyLong());
    }

    @Test
    void findAllByBooker_ShouldReturnAllBookings() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);
        when(bookingService.findAllByBooker(anyLong(), any(BookingStatus.class))).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()))
                .andExpect(jsonPath("$[0].status").value(bookingDto.getStatus().toString()));

        verify(bookingService).findAllByBooker(anyLong(), any(BookingStatus.class));
    }

    @Test
    void findAllByBooker_WithUnsupportedStatus_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void findAllByOwner_ShouldReturnAllBookings() throws Exception {
        List<BookingDto> bookings = List.of(bookingDto);
        when(bookingService.findAllByOwner(anyLong(), any(BookingStatus.class))).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingDto.getId()))
                .andExpect(jsonPath("$[0].status").value(bookingDto.getStatus().toString()));

        verify(bookingService).findAllByOwner(anyLong(), any(BookingStatus.class));
    }

    @Test
    void findAllByOwner_WithUnsupportedStatus_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }
}