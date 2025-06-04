package ru.practicum.shareit.validation;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exceptions.ValidationException;

import java.time.LocalDateTime;

@Component
public class BookingValidator {

    public void validateBookingCreateDto(BookingDto dto) {
        LocalDateTime now = LocalDateTime.now();

        if (dto.getStart() == null) {
            throw new ValidationException("Дата начала бронирования не может быть пустой");
        }

        if (dto.getEnd() == null) {
            throw new ValidationException("Дата окончания бронирования не может быть пустой");
        }

        if (dto.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }

        if (dto.getEnd().isBefore(dto.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        if (dto.getItemId() == null) {
            throw new ValidationException("ID предмета не может быть пустым");
        }
    }
}