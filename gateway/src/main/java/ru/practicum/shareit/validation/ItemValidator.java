package ru.practicum.shareit.validation;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class ItemValidator {

    public void validateItemDto(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Название предмета не может быть пустым");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Описание предмета не может быть пустым");
        }

        if (dto.getAvailable() == null) {
            throw new ValidationException("Статус доступности предмета не может быть пустым");
        }
    }
}