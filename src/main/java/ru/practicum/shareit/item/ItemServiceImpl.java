package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private long lastId = 0;

    private User getUserOrThrow(Long userId) {
        return UserMapper.toUser(userService.findById(userId));
    }

    private Item getItemOrThrow(Long itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            throw new ItemNotFoundException(String.format("Предмет с id %d не найден", itemId));
        }
        return item;
    }

    private void checkItemOwnership(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException(
                String.format("Пользователь с id %d не является владельцем предмета с id %d", userId, item.getId())
            );
        }
    }

    @Override
    public Item create(Item item, Long userId) {
        User user = getUserOrThrow(userId);
        item.setId(++lastId);
        item.setOwner(user);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long itemId, Long userId) {
        User user = getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);
        checkItemOwnership(existingItem, userId);
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        return itemRepository.save(existingItem);
    }

    @Override
    public Item findById(Long id) {
        return getItemOrThrow(id);
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        getUserOrThrow(userId);
        return itemRepository.findAllByUser(userId);
    }

    @Override
    public void delete(Item item) {
        itemRepository.delete(item);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text);
    }
}