package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private long lastId = 0;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public Item create(Item item, Long userId) {
        if (userService.findById(userId) == null) {
            throw new UserNotFoundException("Пользователь не найден");
        }
        item.setId(++lastId);
        item.setOwner(userService.findById(userId));

        return itemRepository.save(item);
    }

    @Override
    public Item update(Item item, Long itemId, Long userId) {
        if (userService.findById(userId) == null) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null || (existingItem.getOwner() != null && !existingItem.getOwner().getId().equals(userId))) {
            throw new ItemNotFoundException("Вещь не найдена или не принадлежит пользователю");
        }

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
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findAllByUser(Long userId) {
        if (userService.findById(userId) == null) throw new UserNotFoundException("Пользователь не найден");

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
