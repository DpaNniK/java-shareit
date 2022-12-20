package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private Integer id = 1;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(UserDto userDto) {
        userDto.setId(id);
        User user = userRepository.createUser(UserMapper.toUser(userDto));
        id++;
        return user;
    }

    @Override
    public User updateUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            return userRepository.updateNameForUser(UserMapper.toUser(userDto));
        }
        if (userDto.getName() == null) {
            return userRepository.updateEmailForUser(UserMapper.toUser(userDto));
        }
        return userRepository.updateUser(UserMapper.toUser(userDto));
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public void deleteUserById(Integer userId) {
        userRepository.deleteUserById(userId);
    }

    @Override
    public void deleteAllUser() {
        id = 1;
        userRepository.deleteAllUser();
    }
}
