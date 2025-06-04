package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    
    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        List<ItemResponseDto> itemResponses = items.stream()
                .map(item -> ItemResponseDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .requestId(request.getId())
                        .ownerId(item.getOwner().getId())
                        .build())
                .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemResponses)
                .build();
    }
} 