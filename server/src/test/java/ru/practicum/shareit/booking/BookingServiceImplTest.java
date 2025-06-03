package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.BookingAccessDeniedException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingStatusException;
import ru.practicum.shareit.booking.exceptions.ItemUnavailableException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ItemService itemService;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;
    private BookingDto bookingDto;
    private ItemDto itemDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Test User", "test@test.com");
        owner = new User(2L, "Owner", "owner@test.com");
        item = new Item(1L, "Test Item", "Test Description", true, owner, null);
        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(booking.getStart());
        bookingCreateDto.setEnd(booking.getEnd());

        itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), null, null, null, null);
        userDto = new UserDto(user.getId(), user.getName(), user.getEmail());
        bookingDto = new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(), itemDto, userDto, booking.getStatus());
    }

    @Test
    void create_ShouldCreateBooking() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemService.findById(anyLong())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(bookingCreateDto, user.getId());

        assertNotNull(result);
        assertEquals(bookingDto.getItem().getId(), result.getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), result.getBooker().getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowException_WhenItemUnavailable() {
        item.setAvailable(false);
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(itemService.findById(anyLong())).thenReturn(item);

        assertThrows(ItemUnavailableException.class, () -> 
            bookingService.create(bookingCreateDto, user.getId())
        );
    }

    @Test
    void create_ShouldThrowException_WhenOwnerTriesToBook() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(owner.getId(), owner.getName(), owner.getEmail()));
        when(itemService.findById(anyLong())).thenReturn(item);

        assertThrows(BookingAccessDeniedException.class, () -> 
            bookingService.create(bookingCreateDto, owner.getId())
        );
    }

    @Test
    void approve_ShouldApproveBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_ShouldRejectBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_ShouldThrowException_WhenNotOwner() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(BookingAccessDeniedException.class, () -> 
            bookingService.approve(booking.getId(), user.getId(), true)
        );
    }

    @Test
    void approve_ShouldThrowException_WhenBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.approve(999L, owner.getId(), true)
        );
    }

    @Test
    void findById_ShouldReturnBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.findById(booking.getId(), user.getId());

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
        assertEquals(bookingDto.getItem().getId(), result.getItem().getId());
    }

    @Test
    void findById_ShouldThrowException_WhenBookingNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.findById(999L, user.getId())
        );
    }

    @Test
    void findAllByBooker_ShouldReturnBookings() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(user.getId(), user.getName(), user.getEmail()));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.findAllByBooker(user.getId(), BookingStatus.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDto.getId(), result.get(0).getId());
    }

    @Test
    void findAllByOwner_ShouldReturnBookings() {
        when(userService.findById(anyLong())).thenReturn(new UserDto(owner.getId(), owner.getName(), owner.getEmail()));
        when(bookingRepository.findByItemOwnerId(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.findAllByOwner(owner.getId(), BookingStatus.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDto.getId(), result.get(0).getId());
    }
} 