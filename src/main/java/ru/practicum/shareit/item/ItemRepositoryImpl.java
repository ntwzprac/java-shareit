package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private List<Item> items;

    public ItemRepositoryImpl() {
        this.items = new ArrayList<>();
    }

    @Override
    public Item findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Item save(Item item) {
        items.add(item);
        return item;
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        return items.stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public void delete(Item item) {
        items.remove(item);
    }

    @Override
    public List<Item> search(String text) {
        return items.stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) && item.getAvailable())
                .toList();
    }
}
