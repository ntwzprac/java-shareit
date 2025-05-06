package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void create_ValidUser_ShouldSaveUser() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        UserDto savedUser = userService.create(userCreateDto);

        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@test.com", savedUser.getEmail());
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        UserDto savedUser = userService.create(userCreateDto);

        UserDto foundUser = userService.findById(savedUser.getId());
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void findById_NonExistingUser_ShouldThrowException() {
        assertThrows(UserNotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void update_ValidUser_ShouldUpdateUser() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        UserDto savedUser = userService.create(userCreateDto);

        UserCreateDto updateData = new UserCreateDto("Updated Name", "updated@test.com");
        UserDto updatedUser = userService.update(savedUser.getId(), updateData);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    @Test
    void update_NonExistingUser_ShouldThrowException() {
        UserCreateDto updateData = new UserCreateDto("Updated Name", "updated@test.com");
        assertThrows(UserNotFoundException.class, () -> userService.update(999L, updateData));
    }

    @Test
    void delete_ExistingUser_ShouldDeleteUser() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        UserDto savedUser = userService.create(userCreateDto);

        userService.delete(savedUser.getId());
        assertThrows(UserNotFoundException.class, () -> userService.findById(savedUser.getId()));
    }
}