package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@AllArgsConstructor
public class ItemRequestDto {
    private String description;
    private UserDto requestor;
    private LocalDateTime created;
}
