package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserCreateDto userCreateDto);
    UserDto update(Long userId, UserCreateDto userCreateDto);
    UserDto findById(Long id);
    List<UserDto> findAll();
    void delete(Long userId);
}
