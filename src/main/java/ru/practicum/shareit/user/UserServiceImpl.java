package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.EmailNotGivenException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private long lastId = 0;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        if (user.getEmail() == null) {
            throw new EmailNotGivenException("Email не задан");
        }

        if (isEmailUsed(user.getEmail())) {
            throw new EmailAlreadyUsedException("Email уже существует");
        }

        user.setId(++lastId);
        return userRepository.save(user);
    }

    @Override
    public User update(Long userId, User user) {
        User oldUser = findById(userId);

        if (user.getEmail() != null && isEmailUsed(user.getEmail())) {
            throw new EmailAlreadyUsedException("Email уже существует");
        }

        if (oldUser == null) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());

        return userRepository.save(oldUser);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    private boolean isEmailUsed(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
