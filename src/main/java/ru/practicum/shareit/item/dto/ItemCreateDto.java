package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemCreateDto {
    @NotNull @NotEmpty
    private String name;
    @NotNull
    private String description;
    @NotNull
    private Boolean available;
}
