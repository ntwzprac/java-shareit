package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

public class ItemMapper {
    public static Item toItem(ItemCreateDto itemCreateDto) {
        Item item = new Item(
            null,
            itemCreateDto.getName(),
            itemCreateDto.getDescription(),
            itemCreateDto.getAvailable(),
            null,
            null
        );
        
        if (itemCreateDto.getRequestId() != null) {
            ItemRequest request = new ItemRequest();
            request.setId(itemCreateDto.getRequestId());
            item.setRequest(request);
        }
        
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getAvailable(),
            null,
            null,
            null,
            item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item(
            itemDto.getId(),
            itemDto.getName(),
            itemDto.getDescription(),
            itemDto.getAvailable(),
            null,
            null
        );
        
        if (itemDto.getRequestId() != null) {
            ItemRequest request = new ItemRequest();
            request.setId(itemDto.getRequestId());
            item.setRequest(request);
        }
        
        return item;
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