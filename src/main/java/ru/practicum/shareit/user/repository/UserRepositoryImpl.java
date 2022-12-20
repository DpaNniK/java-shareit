package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {
    HashMap<Integer, User> userMap = new HashMap<>();

    @Override
    public User createUser(User user) {
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateEmailForUser(User user) {
        userMap.get(user.getId()).setEmail(user.getEmail());
        return userMap.get(user.getId());
    }

    @Override
    public User updateNameForUser(User user) {
        userMap.get(user.getId()).setName(user.getName());
        return userMap.get(user.getId());
    }

    @Override
    public User getUserById(Integer userId) {
        return userMap.get(userId);
    }

    @Override
    public Collection<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public Map<Integer, User> getUsersMap() {
        return userMap;
    }

    @Override
    public void deleteUserById(Integer userId) {
        userMap.remove(userId);
    }

    @Override
    public void deleteAllUser() {
        userMap.clear();
    }
}
