package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {
    private ItemService itemService;
    private ItemRepository itemRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userService = mock(UserService.class);
        itemService = new ItemServiceImpl(itemRepository, userService);
    }

    @Test
    void create_ValidItem_ShouldCreateItem() {
        User owner = new User(1L, "Test User", "test@test.com");
        UserDto ownerDto = new UserDto(1L, "Test User", "test@test.com");
        Item item = new Item(null, "Test Item", "Test Description", true, null, null);
        Item savedItem = new Item(1L, "Test Item", "Test Description", true, owner, null);

        when(userService.findById(1L)).thenReturn(ownerDto);
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        Item createdItem = itemService.create(item, 1L);

        assertNotNull(createdItem.getId());
        assertEquals("Test Item", createdItem.getName());
        assertEquals("Test Description", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());
        assertEquals(owner, createdItem.getOwner());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_NonExistingUser_ShouldThrowException() {
        Item item = new Item(null, "Test Item", "Test Description", true, null, null);

        when(userService.findById(1L)).thenThrow(new UserNotFoundException("Пользователь не найден"));

        assertThrows(UserNotFoundException.class, () -> itemService.create(item, 1L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ValidItem_ShouldUpdateItem() {
        User owner = new User(1L, "Test User", "test@test.com");
        UserDto ownerDto = new UserDto(1L, "Test User", "test@test.com");
        Item existingItem = new Item(1L, "Original Name", "Original Description", true, owner, null);
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);
        Item updatedItem = new Item(1L, "Updated Name", "Updated Description", false, owner, null);

        when(userService.findById(1L)).thenReturn(ownerDto);
        when(itemRepository.findById(1L)).thenReturn(existingItem);
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item result = itemService.update(updateData, 1L, 1L);

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_NonExistingItem_ShouldThrowException() {
        User owner = new User(1L, "Test User", "test@test.com");
        UserDto ownerDto = new UserDto(1L, "Test User", "test@test.com");
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);

        when(userService.findById(1L)).thenReturn(ownerDto);
        when(itemRepository.findById(1L)).thenReturn(null);

        assertThrows(ItemNotFoundException.class, () -> itemService.update(updateData, 1L, 1L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ItemNotOwnedByUser_ShouldThrowException() {
        User owner = new User(1L, "Test User", "test@test.com");
        User otherUser = new User(2L, "Other User", "other@test.com");
        UserDto otherUserDto = new UserDto(2L, "Other User", "other@test.com");
        Item existingItem = new Item(1L, "Original Name", "Original Description", true, owner, null);
        Item updateData = new Item(null, "Updated Name", "Updated Description", false, null, null);

        when(userService.findById(2L)).thenReturn(otherUserDto);
        when(itemRepository.findById(1L)).thenReturn(existingItem);

        assertThrows(ItemAccessDeniedException.class, () -> itemService.update(updateData, 1L, 2L));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void findById_ExistingItem_ShouldReturnItem() {
        Item item = new Item(1L, "Test Item", "Test Description", true, null, null);
        when(itemRepository.findById(1L)).thenReturn(item);

        Item foundItem = itemService.findById(1L);
        assertNotNull(foundItem);
        assertEquals(1L, foundItem.getId());
        assertEquals("Test Item", foundItem.getName());
    }

    @Test
    void findAllByUser_ShouldReturnUserItems() {
        User owner = new User(1L, "Test User", "test@test.com");
        UserDto ownerDto = new UserDto(1L, "Test User", "test@test.com");
        Item item1 = new Item(1L, "Item 1", "Description 1", true, owner, null);
        Item item2 = new Item(2L, "Item 2", "Description 2", true, owner, null);

        when(userService.findById(1L)).thenReturn(ownerDto);
        when(itemRepository.findAllByUser(1L)).thenReturn(List.of(item1, item2));

        List<Item> userItems = itemService.findAllByUser(1L);
        assertEquals(2, userItems.size());
        assertEquals("Item 1", userItems.get(0).getName());
        assertEquals("Item 2", userItems.get(1).getName());
    }

    @Test
    void findAllByUser_NonExistingUser_ShouldThrowException() {
        when(userService.findById(1L)).thenThrow(new UserNotFoundException("Пользователь не найден"));
        assertThrows(UserNotFoundException.class, () -> itemService.findAllByUser(1L));
    }

    @Test
    void search_ShouldReturnMatchingItems() {
        Item item1 = new Item(1L, "Test Item", "Description", true, null, null);
        Item item2 = new Item(2L, "Another Item", "Description", true, null, null);

        when(itemRepository.search("test")).thenReturn(List.of(item1));

        List<Item> searchResults = itemService.search("test");
        assertEquals(1, searchResults.size());
        assertEquals("Test Item", searchResults.get(0).getName());
    }

    @Test
    void search_EmptyText_ShouldReturnEmptyList() {
        List<Item> searchResults = itemService.search("");
        assertTrue(searchResults.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }
}