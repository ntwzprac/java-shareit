package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingValidationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingValidationService bookingValidationService;

    @Test
    void hasUserBookedItem_ShouldReturnTrue_WhenUserHasBookedItem() {
        User user = new User(1L, "Test User", "test@test.com");
        Item item = new Item(1L, "Test Item", "Test Description", true, user, null);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                any(Long.class),
                any(Long.class),
                any(BookingStatus.class),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(booking));

        boolean result = bookingValidationService.hasUserBookedItem(user.getId(), item.getId());

        assertTrue(result);
    }

    @Test
    void hasUserBookedItem_ShouldReturnFalse_WhenUserHasNotBookedItem() {
        when(bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                any(Long.class),
                any(Long.class),
                any(BookingStatus.class),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        boolean result = bookingValidationService.hasUserBookedItem(1L, 1L);

        assertFalse(result);
    }
} 