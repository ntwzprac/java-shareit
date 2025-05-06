package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static Item toItem(ItemCreateDto itemCreateDto) {
        return new Item(
            null,
            itemCreateDto.getName(),
            itemCreateDto.getDescription(),
            itemCreateDto.getAvailable(),
            null,
            null
        );
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getAvailable()
        );
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(
            itemDto.getId(),
            itemDto.getName(),
            itemDto.getDescription(),
            itemDto.getAvailable(),
            null,
            null
        );
    }

    public static Item toItem(ItemUpdateDto itemUpdateDto) {
        return new Item(
            null,
            itemUpdateDto.getName(),
            itemUpdateDto.getDescription(),
            itemUpdateDto.getAvailable(),
            null,
            null
        );
    }
}