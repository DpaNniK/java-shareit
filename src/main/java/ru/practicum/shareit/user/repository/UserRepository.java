package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    User createUser(User user);

    User updateUser(User user);

    User updateEmailForUser(User user);

    User updateNameForUser(User user);

    User getUserById(Integer userId);

    Collection<User> getAllUsers();

    void deleteUserById(Integer userId);

    void deleteAllUser();
}
