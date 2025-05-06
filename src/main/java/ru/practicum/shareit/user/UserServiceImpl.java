package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserCreateDto userCreateDto) {
        User userWithSameEmail = userRepository.findByEmail(userCreateDto.getEmail());
        if (userWithSameEmail != null) {
            throw new EmailAlreadyUsedException("Email уже используется");
        }

        User user = UserMapper.toUser(userCreateDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto update(Long userId, UserCreateDto userCreateDto) {
        User existingUser = UserMapper.toUser(findById(userId));

        if (userCreateDto.getEmail() != null && !userCreateDto.getEmail().equals(existingUser.getEmail())) {
            User userWithSameEmail = userRepository.findByEmail(userCreateDto.getEmail());
            if (userWithSameEmail != null) {
                throw new EmailAlreadyUsedException("Email уже используется");
            }
            existingUser.setEmail(userCreateDto.getEmail());
        }

        if (userCreateDto.getName() != null) {
            existingUser.setName(userCreateDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
            .map(UserMapper::toUserDto)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        userRepository.delete(userId);
    }
}
