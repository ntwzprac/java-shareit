package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

@Data
@AllArgsConstructor
public class ItemCreateDto {
    @NotNull @NotEmpty
    private String name;
    @NotNull
    private String description;
    @NotNull
    private Boolean available;

    public static Item toItem(ItemCreateDto itemCreateDto) {
        return new Item(null, itemCreateDto.getName(), itemCreateDto.getDescription(), itemCreateDto.getAvailable(), null, null);
    }
}
