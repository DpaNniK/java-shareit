package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    User createUser(UserDto user);

    User updateUser(UserDto user);

    User getUserById(Integer userId);

    Collection<User> getAllUsers();

    void deleteUserById(Integer userId);

    void deleteAllUser();
}
