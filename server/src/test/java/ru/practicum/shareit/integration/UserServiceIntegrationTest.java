package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        userCreateDto = new UserCreateDto("Test User", "test@test.com");
    }

    @Test
    void createUser_ShouldCreateNewUser() {
        UserDto createdUser = userService.create(userCreateDto);

        assertNotNull(createdUser.getId());
        assertEquals(userCreateDto.getName(), createdUser.getName());
        assertEquals(userCreateDto.getEmail(), createdUser.getEmail());
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        userService.create(userCreateDto);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.create(userCreateDto));
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        UserDto createdUser = userService.create(userCreateDto);
        UserUpdateDto updateDto = new UserUpdateDto("Updated Name", "updated@test.com");

        UserDto updatedUser = userService.update(createdUser.getId(), updateDto);

        assertEquals(updateDto.getName(), updatedUser.getName());
        assertEquals(updateDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowException() {
        UserUpdateDto updateDto = new UserUpdateDto("Updated Name", "updated@test.com");

        assertThrows(UserNotFoundException.class, () -> userService.update(999L, updateDto));
    }

    @Test
    void findById_ShouldReturnUser() {
        UserDto createdUser = userService.create(userCreateDto);

        UserDto foundUser = userService.findById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getName(), foundUser.getName());
        assertEquals(createdUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void findById_WithNonExistentId_ShouldThrowException() {
        assertThrows(UserNotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        UserDto user1 = userService.create(userCreateDto);
        UserDto user2 = userService.create(new UserCreateDto("Test User 2", "test2@test.com"));

        List<UserDto> users = userService.findAll();

        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(user1.getId())));
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(user2.getId())));
    }

    @Test
    void delete_ShouldDeleteUser() {
        UserDto createdUser = userService.create(userCreateDto);

        userService.delete(createdUser.getId());

        assertThrows(UserNotFoundException.class, () -> userService.findById(createdUser.getId()));
    }

    @Test
    void delete_WithNonExistentId_ShouldThrowException() {
        assertThrows(UserNotFoundException.class, () -> userService.delete(999L));
    }
}