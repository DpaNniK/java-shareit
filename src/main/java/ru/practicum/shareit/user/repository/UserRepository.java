package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserRepository {
    User createUser(User user);

    User updateUser(User user);

    User updateEmailForUser(User user);

    User updateNameForUser(User user);

    User getUserById(Integer userId);

    Collection<User> getAllUsers();

    Map<Integer, User> getUsersMap();

    void deleteUserById(Integer userId);

    void deleteAllUser();
}
