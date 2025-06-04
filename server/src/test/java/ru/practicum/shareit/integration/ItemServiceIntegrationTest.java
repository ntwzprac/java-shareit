package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto user;
    private Item item;

    @BeforeEach
    void setUp() {
        user = userService.create(new UserCreateDto("Test User", "test@test.com"));
        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
    }

    @Test
    void createItem_ShouldCreateNewItem() {
        Item createdItem = itemService.create(item, user.getId());

        assertNotNull(createdItem.getId());
        assertEquals(item.getName(), createdItem.getName());
        assertEquals(item.getDescription(), createdItem.getDescription());
        assertEquals(item.getAvailable(), createdItem.getAvailable());
    }

    @Test
    void updateItem_ShouldUpdateExistingItem() {
        Item createdItem = itemService.create(item, user.getId());

        Item updateItem = new Item();
        updateItem.setName("Updated Name");
        updateItem.setDescription("Updated Description");

        Item updatedItem = itemService.update(updateItem, createdItem.getId(), user.getId());

        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertEquals(createdItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void findAllByUser_ShouldReturnUserItems() {
        Item createdItem = itemService.create(item, user.getId());

        List<Item> userItems = itemService.findAllByUser(user.getId());

        assertFalse(userItems.isEmpty());
        assertEquals(createdItem.getId(), userItems.get(0).getId());
    }

    @Test
    void search_ShouldFindItemsByText() {
        Item createdItem = itemService.create(item, user.getId());

        List<Item> searchResults = itemService.search("Test");

        assertFalse(searchResults.isEmpty());
        assertEquals(createdItem.getId(), searchResults.get(0).getId());
    }
}