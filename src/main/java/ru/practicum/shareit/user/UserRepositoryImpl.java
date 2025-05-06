package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User findById(Long id) {
        return users.get(id);
    }

    @Override
    public User findByEmail(String email) {
        return users.values().stream()
            .filter(user -> user.getEmail().equals(email))
            .findFirst()
            .orElse(null);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

    @Override
    public List<User> findAll() {
        return users.values().stream().collect(Collectors.toList());
    }
}
