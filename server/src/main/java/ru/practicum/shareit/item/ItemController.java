package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestBody ItemCreateDto item, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemMapper.toItemDto(itemService.create(ItemMapper.toItem(item), userId));
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }
        return itemService.addComment(itemId, userId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getEnrichedItemDto(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> findAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findAllEnrichedByUser(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemUpdateDto item, @PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ItemMapper.toItemDto(itemService.update(ItemMapper.toItem(item), itemId, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
