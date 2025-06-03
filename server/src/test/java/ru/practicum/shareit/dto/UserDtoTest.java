package ru.practicum.shareit.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializeUserDto() throws Exception {
        UserDto userDto = new UserDto(1L, "Test User", "test@test.com");

        String json = objectMapper.writeValueAsString(userDto);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Test User\""));
        assertTrue(json.contains("\"email\":\"test@test.com\""));
    }

    @Test
    void deserializeUserDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@test.com\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertNotNull(userDto);
        assertEquals(1L, userDto.getId());
        assertEquals("Test User", userDto.getName());
        assertEquals("test@test.com", userDto.getEmail());
    }
}