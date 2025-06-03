package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeItemRequestDto() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Test Request", now, List.of());
        String json = objectMapper.writeValueAsString(requestDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Test Request\"");
        assertThat(json).contains("\"items\":[]");

        ItemRequestDto deserializedDto = objectMapper.readValue(json, ItemRequestDto.class);
        assertThat(deserializedDto.getCreated()).isEqualTo(now);
    }

    @Test
    void deserializeItemRequestDto() throws Exception {
        String json = "{\"id\":1,\"description\":\"Test Request\",\"created\":\"2024-03-20T10:00:00\",\"items\":[]}";
        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Test Request");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.parse("2024-03-20T10:00:00"));
        assertThat(requestDto.getItems()).isEmpty();
    }

    @Test
    void deserializeItemRequestDtoWithNullFields() throws Exception {
        String json = "{\"id\":1,\"description\":\"Test Request\",\"created\":\"2024-03-20T10:00:00\",\"items\":null}";
        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Test Request");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.parse("2024-03-20T10:00:00"));
        assertThat(requestDto.getItems()).isNull();
    }
}