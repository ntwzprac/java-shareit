package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void create_ValidUser_ShouldSaveUser() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        User user = new User(null, "Test User", "test@test.com");
        User savedUser = new User(1L, "Test User", "test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.create(userCreateDto);

        assertNotNull(result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_UserWithExistingEmail_ShouldThrowException() {
        UserCreateDto userCreateDto = new UserCreateDto("Test User", "test@test.com");
        User existingUser = new User(1L, "Existing User", "test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(existingUser);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.create(userCreateDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        User user = new User(1L, "Test User", "test@test.com");
        when(userRepository.getUserById(1L)).thenReturn(user);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void findById_NonExistingUser_ShouldThrowException() {
        when(userRepository.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void update_ValidUser_ShouldUpdateUser() {
        User existingUser = new User(1L, "Original Name", "original@test.com");
        User updatedUser = new User(1L, "Updated Name", "updated@test.com");
        UserUpdateDto updateData = new UserUpdateDto("Updated Name", "updated@test.com");

        when(userRepository.getUserById(1L)).thenReturn(existingUser);
        when(userRepository.findByEmail("updated@test.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateData);

        assertEquals("Updated Name", result.getName());
        assertEquals("updated@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_NonExistingUser_ShouldThrowException() {
        UserUpdateDto updateData = new UserUpdateDto("Updated Name", "updated@test.com");
        when(userRepository.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.update(999L, updateData));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_UserWithExistingEmail_ShouldThrowException() {
        User existingUser = new User(1L, "Original Name", "original@test.com");
        User otherUser = new User(2L, "Other User", "updated@test.com");
        UserUpdateDto updateData = new UserUpdateDto("Updated Name", "updated@test.com");

        when(userRepository.getUserById(1L)).thenReturn(existingUser);
        when(userRepository.findByEmail("updated@test.com")).thenReturn(otherUser);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.update(1L, updateData));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        List<User> users = List.of(
            new User(1L, "User 1", "user1@test.com"),
            new User(2L, "User 2", "user2@test.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("User 1", result.get(0).getName());
        assertEquals("User 2", result.get(1).getName());
    }

    @Test
    void delete_ExistingUser_ShouldDeleteUser() {
        User user = new User(1L, "Test User", "test@test.com");
        when(userRepository.getUserById(1L)).thenReturn(user);

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void delete_NonExistingUser_ShouldThrowException() {
        when(userRepository.getUserById(999L)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> userService.delete(999L));
        verify(userRepository, never()).delete(any(User.class));
    }
}