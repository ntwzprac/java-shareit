package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ItemCreateDto itemCreateDto;
    private ItemUpdateDto itemUpdateDto;
    private ItemDto itemDto;
    private CommentDto commentDto;
    private Item item;

    @BeforeEach
    void setUp() {
        itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("Test Item");
        itemCreateDto.setDescription("Test Description");
        itemCreateDto.setAvailable(true);

        itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Updated Item");
        itemUpdateDto.setDescription("Updated Description");

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("Test Comment");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
    }

    @Test
    void create_ShouldCreateItem() {
        when(itemService.create(any(Item.class), anyLong())).thenReturn(item);

        ItemDto response = itemController.create(itemCreateDto, 1L);

        assertNotNull(response);
        assertEquals(itemDto.getName(), response.getName());
        verify(itemService).create(any(Item.class), eq(1L));
    }

    @Test
    void update_ShouldUpdateItem() {
        when(itemService.update(any(Item.class), anyLong(), anyLong())).thenReturn(item);

        ItemDto response = itemController.update(itemUpdateDto, 1L, 1L);

        assertNotNull(response);
        assertEquals(itemDto.getName(), response.getName());
        verify(itemService).update(any(Item.class), eq(1L), eq(1L));
    }

    @Test
    void update_ShouldReturnNotFound_WhenItemNotFound() {
        when(itemService.update(any(Item.class), anyLong(), anyLong()))
                .thenThrow(new ItemNotFoundException("Item not found"));

        assertThrows(ItemNotFoundException.class, () ->
                itemController.update(itemUpdateDto, 1L, 1L)
        );
    }

    @Test
    void update_ShouldReturnForbidden_WhenUserIsNotOwner() {
        when(itemService.update(any(Item.class), anyLong(), anyLong()))
                .thenThrow(new ItemAccessDeniedException("Access denied"));

        assertThrows(ItemAccessDeniedException.class, () ->
                itemController.update(itemUpdateDto, 1L, 1L)
        );
    }

    @Test
    void findById_ShouldReturnItem() {
        when(itemService.getEnrichedItemDto(anyLong(), anyLong())).thenReturn(itemDto);

        ItemDto response = itemController.findById(1L, 1L);

        assertNotNull(response);
        assertEquals(itemDto.getId(), response.getId());
        verify(itemService).getEnrichedItemDto(eq(1L), eq(1L));
    }

    @Test
    void findAllByUser_ShouldReturnItems() {
        when(itemService.findAllEnrichedByUser(anyLong())).thenReturn(List.of(itemDto));

        List<ItemDto> response = itemController.findAllByUser(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(itemDto.getId(), response.get(0).getId());
        verify(itemService).findAllEnrichedByUser(eq(1L));
    }

    @Test
    void search_ShouldReturnItems() {
        when(itemService.search(anyString())).thenReturn(List.of(item));

        List<ItemDto> response = itemController.search("test");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(itemDto.getName(), response.get(0).getName());
        verify(itemService).search(eq("test"));
    }

    @Test
    void addComment_ShouldAddComment() {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);

        CommentDto response = itemController.addComment(1L, 1L, commentDto);

        assertNotNull(response);
        assertEquals(commentDto.getText(), response.getText());
        verify(itemService).addComment(eq(1L), eq(1L), eq(commentDto));
    }

    @Test
    void addComment_ShouldThrowException_WhenCommentTextIsBlank() {
        commentDto.setText("   ");

        assertThrows(IllegalArgumentException.class, () ->
                itemController.addComment(1L, 1L, commentDto)
        );
    }
} 