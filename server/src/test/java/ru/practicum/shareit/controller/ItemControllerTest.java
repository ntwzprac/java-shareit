package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.ItemAccessDeniedException;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemCreateDto itemCreateDto;
    private ItemUpdateDto itemUpdateDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);
        itemCreateDto = new ItemCreateDto("Test Item", "Test Description", true, null);
        itemUpdateDto = new ItemUpdateDto("Updated Item", "Updated Description", true);
        commentDto = new CommentDto(1L, "Test Comment", "Test Author", null);
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        Item item = ItemMapper.toItem(itemCreateDto);
        when(itemService.create(any(Item.class), anyLong())).thenReturn(item);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemCreateDto.getName()))
                .andExpect(jsonPath("$.description").value(itemCreateDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemCreateDto.getAvailable()));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        Item item = ItemMapper.toItem(itemUpdateDto);
        when(itemService.update(any(Item.class), anyLong(), anyLong())).thenReturn(item);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemUpdateDto.getName()))
                .andExpect(jsonPath("$.description").value(itemUpdateDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemUpdateDto.getAvailable()));
    }

    @Test
    void getItem_ShouldReturnItem() throws Exception {
        when(itemService.getEnrichedItemDto(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    void findAllByUser_ShouldReturnUserItems() throws Exception {
        when(itemService.findAllEnrichedByUser(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()));
    }

    @Test
    void search_ShouldReturnFoundItems() throws Exception {
        when(itemService.search(any())).thenReturn(List.of(ItemMapper.toItem(itemDto)));

        mockMvc.perform(get("/items/search")
                        .param("text", "test")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()));
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
    }

    @Test
    void addComment_ShouldReturnBadRequest_WhenCommentTextIsBlank() throws Exception {
        commentDto.setText("   ");

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturnItem() throws Exception {
        when(itemService.getEnrichedItemDto(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    void findAllByUser_ShouldReturnItems() throws Exception {
        when(itemService.findAllEnrichedByUser(anyLong())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()));
    }

    @Test
    void update_ShouldReturnUpdatedItem() throws Exception {
        Item item = ItemMapper.toItem(itemUpdateDto);
        when(itemService.update(any(Item.class), anyLong(), anyLong())).thenReturn(item);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemUpdateDto.getName()))
                .andExpect(jsonPath("$.description").value(itemUpdateDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemUpdateDto.getAvailable()));
    }

    @Test
    void search_ShouldReturnItems() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(ItemMapper.toItem(itemDto)));

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()));
    }

    @Test
    void findById_ShouldReturnNotFound_WhenItemNotFound() throws Exception {
        when(itemService.getEnrichedItemDto(anyLong(), anyLong()))
                .thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        when(itemService.update(any(Item.class), anyLong(), anyLong()))
                .thenThrow(new ItemAccessDeniedException("User is not the owner"));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addComment_ShouldReturnBadRequest_WhenCommentNotAllowed() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new CommentNotAllowedException("User cannot comment on this item"));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_ShouldReturnEmptyList_WhenTextIsBlank() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "   ")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}