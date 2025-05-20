package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

@Service
@RequiredArgsConstructor
public class BookingValidationService {
    private final BookingRepository bookingRepository;

    public boolean hasUserBookedItem(Long userId, Long itemId) {
        return bookingRepository.findByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, 
                itemId, 
                BookingStatus.APPROVED,
                java.time.LocalDateTime.now()
        ).isPresent();
    }
} 