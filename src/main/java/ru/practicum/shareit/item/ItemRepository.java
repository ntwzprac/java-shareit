package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item findById(Long id);
    Item save(Item item);
    List<Item> findAllByUser(Long userId);
    void delete(Item item);
    List<Item> search(String text);
}
