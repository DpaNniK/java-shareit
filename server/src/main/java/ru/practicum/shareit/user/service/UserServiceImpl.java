package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private Integer id = 1;

    @Override
    public User createUser(UserDto userDto) {
        userDto.setId(id);
        log.info("Создан новый пользователь с id = {}", id);
        id++;
        return userRepository.save(UserMapper.toUser(userDto));
    }

    @Override
    public User updateUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (checkContainsIdInUserMap(user.getId())) {
            if (userDto.getEmail() == null) {
                log.info("Обновлено имя для пользователя с id = {}", user.getId());
                userRepository.updateUserName(user.getName(), user.getId());
                User updateUser = getUserById(user.getId());
                updateUser.setName(user.getName());
                return updateUser;
            }
            if (userDto.getName() == null) {
                log.info("Пользователь с id = {} обновил email на {}", user.getId(), user.getEmail());
                userRepository.updateUserEmail(user.getEmail(), user.getId());
                User updateUser = getUserById(user.getId());
                updateUser.setEmail(user.getEmail());
                return updateUser;
            }
            log.info("Обновлена информация о пользователе с id = {}", user.getId());
            return userRepository.save(UserMapper.toUser(userDto));
        }
        log.warn("Ошибка при обновлении пользователя. Пользователь с таким id не найден");
        throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
    }

    @Override
    public User getUserById(Integer userId) {
        log.info("Запрошен пользователь с id = {}", userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Ошибка при получении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь с таким id не найден");
        }
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return userRepository.findAll();
    }

    @Override
    public void deleteUserById(Integer userId) {
        if (!checkContainsIdInUserMap(userId)) {
            log.warn("Ошибка при удалении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
        }
        log.info("Пользователь с id = {} удален", userId);
        userRepository.deleteById(userId);
    }

    @Override
    public void deleteAllUser() {
        userRepository.deleteAll();
    }

    private boolean checkContainsIdInUserMap(Integer id) {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, user1 -> user1)).containsKey(id);
    }
}
