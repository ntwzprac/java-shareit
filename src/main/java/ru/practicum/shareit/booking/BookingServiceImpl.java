package ru.practicum.shareit.booking;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(ItemService itemService, UserService userService, BookingRepository bookingRepository) {
        this.itemService = itemService;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingDto create(BookingCreateDto bookingCreateDto, Long userId) {
        User user = UserMapper.toUser(userService.findById(userId));
        Item item = itemService.findById(bookingCreateDto.getItemId());

        if (!Item.isAvailable(item)) {
            throw new ItemUnavailableException(String.format("Предмет с id %d недоступен для бронирования", item.getId()));
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new BookingAccessDeniedException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingCreateDto, item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
    }

    @Override
    public BookingDto approve(Long bookingId, Long userId, boolean approved) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingAccessDeniedException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingStatusException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingAccessDeniedException("Доступ к бронированию запрещен");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAllByBooker(Long userId, BookingStatus state) {
        userService.findById(userId);
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("start").descending());
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageRequest);
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, state, pageRequest);
                break;
            default:
                throw new BookingStatusException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findAllByOwner(Long userId, BookingStatus state) {
        userService.findById(userId);
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("start").descending());
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(userId, pageRequest);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndCurrent(userId, now, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndPast(userId, now, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndFuture(userId, now, pageRequest);
                break;
            case WAITING:
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, state, pageRequest);
                break;
            default:
                throw new BookingStatusException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}