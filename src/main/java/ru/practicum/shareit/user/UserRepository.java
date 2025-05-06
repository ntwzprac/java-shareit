package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User findById(Long id);

    User findByEmail(String email);

    User save(User user);

    void delete(Long userId);

    List<User> findAll();
}
