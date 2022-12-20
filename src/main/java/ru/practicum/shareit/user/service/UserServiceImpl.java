package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Slf4j
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
        if (checkNotDuplicateEmail(UserMapper.toUser(userDto))) {
            id++;
            log.info("Создан новый пользователь с id = {}", id);
            return userRepository.createUser(UserMapper.toUser(userDto));
        }
        log.warn("Ошибка при создании пользователя. Пользователь с таким email уже существует");
        throw new RequestError(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");

    }

    @Override
    public User updateUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (!checkNotDuplicateEmail(user)) {
            log.warn("Ошибка при обновлении email пользователя. Пользователь с таким email уже существует");
            throw new RequestError(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }
        if (checkContainsIdInUserMap(user.getId())) {
            if (userDto.getEmail() == null) {
                log.info("Обновлено имя для пользователя с id = {}", user.getId());
                return userRepository.updateNameForUser(UserMapper.toUser(userDto));
            }
            if (userDto.getName() == null) {
                log.info("Пользователь с id = {} обновил email на {}", user.getId(), user.getEmail());
                return userRepository.updateEmailForUser(UserMapper.toUser(userDto));
            }
            log.info("Обновлена информация о пользователе с id = {}", user.getId());
            return userRepository.updateUser(UserMapper.toUser(userDto));
        }
        log.warn("Ошибка при обновлении пользователя. Пользователь с таким id не найден");
        throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
    }

    @Override
    public User getUserById(Integer userId) {
        if (checkContainsIdInUserMap(userId)) {
            log.info("Запрошен пользователь с id = {}", userId);
            return userRepository.getUserById(userId);
        }
        log.warn("Ошибка при получении пользователя. Пользователь с таким id не найден");
        throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь с таким id не найден");
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return userRepository.getAllUsers();
    }

    @Override
    public void deleteUserById(Integer userId) {
        if (!checkContainsIdInUserMap(userId)) {
            log.warn("Ошибка при удалении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
        }
        log.info("Пользователь с id = {} удален", userId);
        userRepository.deleteUserById(userId);
    }

    @Override
    public void deleteAllUser() {
        this.id = 1;
        userRepository.deleteAllUser();
    }

    private boolean checkNotDuplicateEmail(User user) {
        return userRepository.getUsersMap().values().stream().noneMatch(mapUser -> mapUser.getEmail()
                .equals(user.getEmail()));
    }

    private boolean checkContainsIdInUserMap(Integer id) {
        return userRepository.getUsersMap().containsKey(id);
    }
}
