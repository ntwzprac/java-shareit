package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.validation.BookingValidator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;
	private final BookingValidator bookingValidator;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
			@Valid @RequestBody BookingDto bookingDto) {
		log.info("Creating booking {} for user {}", bookingDto, userId);
		bookingValidator.validateBookingCreateDto(bookingDto);
		return bookingClient.createBooking(userId, bookingDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBooking(@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable long bookingId,
			@RequestParam boolean approved) {
		log.info("Updating booking {} for user {} with approved={}", bookingId, userId, approved);
		return bookingClient.updateBooking(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable long bookingId) {
		log.info("Getting booking {} for user {}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		log.info("Getting bookings for user {} with state={}, from={}, size={}", userId, state, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		log.info("Getting owner bookings for user {} with state={}, from={}, size={}", userId, state, from, size);
		return bookingClient.getOwnerBookings(userId, state, from, size);
	}
}
