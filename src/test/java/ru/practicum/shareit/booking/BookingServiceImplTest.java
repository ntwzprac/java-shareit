package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {
    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private ItemService itemService;
    private UserService userService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        itemService = mock(ItemService.class);
        userService = mock(UserService.class);
        bookingService = new BookingServiceImpl(itemService, userService, bookingRepository);

        owner = new User(1L, "Owner", "owner@test.com");
        booker = new User(2L, "Booker", "booker@test.com");
        item = new Item(1L, "Test Item", "Description", true, owner, null);
        booking = new Booking(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2),
                item, booker, BookingStatus.WAITING);
        bookingCreateDto = new BookingCreateDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));
    }

    @Test
    void create_ValidBooking_ShouldCreateBooking() {
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(itemService.findById(1L)).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(bookingCreateDto, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_BookingOwnItem_ShouldThrowException() {
        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(itemService.findById(1L)).thenReturn(item);

        assertThrows(BookingAccessDeniedException.class, () -> bookingService.create(bookingCreateDto, 1L));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_UnavailableItem_ShouldThrowException() {
        Item unavailableItem = new Item(1L, "Test Item", "Description", false, owner, null);
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(itemService.findById(1L)).thenReturn(unavailableItem);

        assertThrows(ItemUnavailableException.class, () -> bookingService.create(bookingCreateDto, 2L));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_ValidBooking_ShouldApproveBooking() {
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_NonOwnerApproval_ShouldThrowException() {
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        assertThrows(BookingAccessDeniedException.class, () -> bookingService.approve(1L, 2L, true));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_AlreadyApprovedBooking_ShouldThrowException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        assertThrows(BookingStatusException.class, () -> bookingService.approve(1L, 1L, true));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void findById_ValidBooking_ShouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        BookingDto result = bookingService.findById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_NonParticipantAccess_ShouldThrowException() {
        User otherUser = new User(3L, "Other", "other@test.com");
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));
        when(userService.findById(3L)).thenReturn(new UserDto(3L, "Other", "other@test.com"));

        assertThrows(BookingAccessDeniedException.class, () -> bookingService.findById(1L, 3L));
    }

    @Test
    void findAllByBooker_ShouldReturnBookings() {
        List<Booking> bookings = List.of(booking);
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), any())).thenReturn(bookings);

        List<BookingDto> result = bookingService.findAllByBooker(2L, BookingStatus.ALL);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findAllByOwner_ShouldReturnBookings() {
        List<Booking> bookings = List.of(booking);
        when(userService.findById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@test.com"));
        when(bookingRepository.findByItemOwnerId(eq(1L), any())).thenReturn(bookings);

        List<BookingDto> result = bookingService.findAllByOwner(1L, BookingStatus.ALL);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findAllByBooker_WithDifferentStates_ShouldReturnFilteredBookings() {
        LocalDateTime now = LocalDateTime.now();
        Booking currentBooking = new Booking(1L, 
            now.minusHours(1),
            now.plusHours(1),
            item, 
            booker, 
            BookingStatus.APPROVED);
        
        List<Booking> currentBookings = List.of(currentBooking);
        when(userService.findById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@test.com"));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), 
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any()))
                .thenReturn(currentBookings);

        List<BookingDto> result = bookingService.findAllByBooker(2L, BookingStatus.CURRENT);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            eq(2L), 
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any());
    }
} 