package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeItemDto() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Test Description", true, null, null, null, null);
        String json = objectMapper.writeValueAsString(itemDto);
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Test Item\"");
        assertThat(json).contains("\"description\":\"Test Description\"");
        assertThat(json).contains("\"available\":true");
    }

    @Test
    void deserializeItemDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true}";
        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isEqualTo("Test Description");
        assertThat(itemDto.getAvailable()).isTrue();
    }

    @Test
    void deserializeItemDtoWithNullFields() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true,\"comments\":null,\"lastBooking\":null,\"nextBooking\":null,\"requestId\":null}";
        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);
        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Test Item");
        assertThat(itemDto.getDescription()).isEqualTo("Test Description");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getComments()).isNull();
        assertThat(itemDto.getLastBooking()).isNull();
        assertThat(itemDto.getNextBooking()).isNull();
        assertThat(itemDto.getRequestId()).isNull();
    }
}