package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.exceptions.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto requestDto;
    private ItemResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = ItemRequestDto.builder()
                .description("Test Request")
                .build();

        responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(1L)
                .ownerId(1L)
                .build();
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(anyLong(), any(ItemRequestDto.class))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()));
    }

    @Test
    void getUserRequests_ShouldReturnRequests() throws Exception {
        when(itemRequestService.getUserRequests(anyLong())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(requestDto.getDescription()));
    }

    @Test
    void getAllRequests_ShouldReturnRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(requestDto.getDescription()));
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()));
    }

    @Test
    void getRequestById_ShouldReturnNotFound_WhenRequestNotFound() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}