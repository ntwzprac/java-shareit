package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.BookingAccessDeniedException;
import ru.practicum.shareit.booking.exceptions.ItemUnavailableException;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private BookingCreateDto bookingCreateDto;

    @BeforeEach
    void setUp() {
        UserCreateDto ownerCreateDto = new UserCreateDto("Owner", "owner@test.com");
        owner = userService.create(ownerCreateDto);

        UserCreateDto bookerCreateDto = new UserCreateDto("Booker", "booker@test.com");
        booker = userService.create(bookerCreateDto);

        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Test Item");
        itemCreateDto.setDescription("Test Description");
        itemCreateDto.setAvailable(true);
        item = ItemMapper.toItemDto(itemService.create(ItemMapper.toItem(itemCreateDto), owner.getId()));

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(item.getId());
        bookingCreateDto.setStart(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusHours(2));
    }

    @Test
    void createBooking_ValidData_ShouldCreateBooking() {
        BookingDto booking = bookingService.create(bookingCreateDto, booker.getId());

        assertNotNull(booking);
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(item.getId(), booking.getItem().getId());
        assertEquals(booker.getId(), booking.getBooker().getId());
    }

    @Test
    void createBooking_ItemUnavailable_ShouldThrowException() {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto(item.getName(), item.getDescription(), false);
        itemService.update(ItemMapper.toItem(itemUpdateDto), item.getId(), owner.getId());

        assertThrows(ItemUnavailableException.class,
                () -> bookingService.create(bookingCreateDto, booker.getId()));
    }

    @Test
    void createBooking_OwnerTriesToBook_ShouldThrowException() {
        assertThrows(BookingAccessDeniedException.class,
                () -> bookingService.create(bookingCreateDto, owner.getId()));
    }

    @Test
    void approveBooking_ValidData_ShouldApproveBooking() {
        BookingDto booking = bookingService.create(bookingCreateDto, booker.getId());
        BookingDto approvedBooking = bookingService.approve(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approveBooking_NotOwnerTriesToApprove_ShouldThrowException() {
        BookingDto booking = bookingService.create(bookingCreateDto, booker.getId());

        assertThrows(BookingAccessDeniedException.class,
                () -> bookingService.approve(booking.getId(), booker.getId(), true));
    }

    @Test
    void findById_ValidData_ShouldReturnBooking() {
        BookingDto createdBooking = bookingService.create(bookingCreateDto, booker.getId());
        BookingDto foundBooking = bookingService.findById(createdBooking.getId(), booker.getId());

        assertNotNull(foundBooking);
        assertEquals(createdBooking.getId(), foundBooking.getId());
    }

    @Test
    void findById_NotAuthorizedUser_ShouldThrowException() {
        BookingDto booking = bookingService.create(bookingCreateDto, booker.getId());
        UserCreateDto unauthorizedUserCreateDto = new UserCreateDto("Unauthorized", "unauthorized@test.com");
        UserDto unauthorizedUser = userService.create(unauthorizedUserCreateDto);

        assertThrows(BookingAccessDeniedException.class,
                () -> bookingService.findById(booking.getId(), unauthorizedUser.getId()));
    }

    @Test
    void findAllByBooker_ValidData_ShouldReturnBookings() {
        bookingService.create(bookingCreateDto, booker.getId());
        List<BookingDto> bookings = bookingService.findAllByBooker(booker.getId(), BookingStatus.ALL);

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }

    @Test
    void findAllByOwner_ValidData_ShouldReturnBookings() {
        bookingService.create(bookingCreateDto, booker.getId());
        List<BookingDto> bookings = bookingService.findAllByOwner(owner.getId(), BookingStatus.ALL);

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }
}