package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemCreateDto item, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemMapper.toItemDto(itemService.create(ItemMapper.toItem(item), userId));
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        return ItemMapper.toItemDto(itemService.findById(itemId));
    }

    @GetMapping
    public List<ItemDto> findAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findAllByUser(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Valid @RequestBody ItemUpdateDto item, @PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemMapper.toItemDto(itemService.update(ItemMapper.toItem(item), itemId, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
