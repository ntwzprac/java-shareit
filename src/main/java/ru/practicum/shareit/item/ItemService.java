package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Item item, Long userId);
    Item update(Item item, Long itemId, Long userId);
    Item findById(Long id);
    List<Item> findAllByUser(Long userId);
    void delete(Item item);
    List<Item> search(String text);
}
