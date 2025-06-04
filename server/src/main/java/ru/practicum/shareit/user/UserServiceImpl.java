package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.exception.EmailAlreadyUsedException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

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
    public UserDto update(Long userId, UserUpdateDto userUpdateDto) {
        User existingUser = userRepository.getUserById(userId);
        if (existingUser == null) {
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }

        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(existingUser.getEmail())) {
            User userWithSameEmail = userRepository.findByEmail(userUpdateDto.getEmail());
            if (userWithSameEmail != null) {
                throw new EmailAlreadyUsedException("Email уже используется");
            }
            existingUser.setEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.getName() != null) {
            existingUser.setName(userUpdateDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.getUserById(id);
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
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        userRepository.delete(user);
    }
}
