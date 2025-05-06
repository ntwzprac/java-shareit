package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemCreateDto {
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    
    @NotBlank(message = "Описание не может быть пустым")
    private String description;
    
    @NotNull(message = "Статус доступности должен быть указан")
    private Boolean available;
}
