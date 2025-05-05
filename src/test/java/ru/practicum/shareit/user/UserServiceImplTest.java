package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.EmailNotGivenException;
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
    void save_ValidUser_ShouldSaveUser() {
        User user = new User(null, "Test User", "test@test.com");
        User savedUser = userService.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@test.com", savedUser.getEmail());
    }

    @Test
    void save_UserWithoutEmail_ShouldThrowException() {
        User user = new User(null, "Test User", null);

        assertThrows(EmailNotGivenException.class, () -> userService.save(user));
    }

    @Test
    void save_DuplicateEmail_ShouldThrowException() {
        User user1 = new User(null, "Test User 1", "test@test.com");
        User user2 = new User(null, "Test User 2", "test@test.com");

        userService.save(user1);
        assertThrows(EmailAlreadyUsedException.class, () -> userService.save(user2));
    }

    @Test
    void findById_ExistingUser_ShouldReturnUser() {
        User user = new User(null, "Test User", "test@test.com");
        User savedUser = userService.save(user);

        User foundUser = userService.findById(savedUser.getId());
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }

    @Test
    void findById_NonExistingUser_ShouldReturnNull() {
        assertNull(userService.findById(999L));
    }

    @Test
    void update_ValidUser_ShouldUpdateUser() {
        User user = new User(null, "Test User", "test@test.com");
        User savedUser = userService.save(user);

        User updateData = new User(null, "Updated Name", "updated@test.com");
        User updatedUser = userService.update(savedUser.getId(), updateData);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());
    }

    @Test
    void update_NonExistingUser_ShouldThrowException() {
        User updateData = new User(null, "Updated Name", "updated@test.com");
        assertThrows(UserNotFoundException.class, () -> userService.update(999L, updateData));
    }

    @Test
    void delete_ExistingUser_ShouldDeleteUser() {
        User user = new User(null, "Test User", "test@test.com");
        User savedUser = userService.save(user);

        userService.delete(savedUser);
        assertNull(userService.findById(savedUser.getId()));
    }
}