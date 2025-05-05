package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

@Data
@AllArgsConstructor
public class ItemUpdateDto {
    private String name;
    private String description;
    private Boolean available;

    public static Item toItem(ItemUpdateDto itemUpdateDto) {
        return new Item(null, itemUpdateDto.getName(), itemUpdateDto.getDescription(), itemUpdateDto.getAvailable(), null, null);
    }
}
