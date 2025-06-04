package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserCreateDto userCreateDto;
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        userCreateDto = new UserCreateDto("Test User", "test@test.com");
        userDto = new UserDto(1L, "Test User", "test@test.com");
        userUpdateDto = new UserUpdateDto("Updated Name", "updated@test.com");
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.create(any(UserCreateDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService).create(any(UserCreateDto.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldReturnConflict() throws Exception {
        when(userService.create(any(UserCreateDto.class))).thenThrow(new EmailAlreadyUsedException("Email already used"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isConflict());

        verify(userService).create(any(UserCreateDto.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserDto updatedUser = new UserDto(1L, "Updated Name", "updated@test.com");
        when(userService.update(anyLong(), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()));

        verify(userService).update(anyLong(), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.update(anyLong(), any(UserUpdateDto.class))).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound());

        verify(userService).update(anyLong(), any(UserUpdateDto.class));
    }

    @Test
    void findById_ShouldReturnUser() throws Exception {
        when(userService.findById(anyLong())).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService).findById(anyLong());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(userService.findById(anyLong())).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).findById(anyLong());
    }

    @Test
    void findAll_ShouldReturnAllUsers() throws Exception {
        List<UserDto> users = List.of(userDto);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));

        verify(userService).findAll();
    }

    @Test
    void delete_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).delete(anyLong());
    }
}