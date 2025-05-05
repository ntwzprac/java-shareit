package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

public interface UserRepository {
    User findById(Long id);

    User findByEmail(String email);

    User save(User user);

    void delete(User user);
}
