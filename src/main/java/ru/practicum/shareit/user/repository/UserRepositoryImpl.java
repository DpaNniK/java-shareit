package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    HashMap<Integer, User> userMap = new HashMap<>();

    @Override
    public User createUser(User user) {
        if (checkNotDuplicateEmail(user)) {
            userMap.put(user.getId(), user);
            log.info("Создан новый пользователь с id = {}", user.getId());
            return user;
        }
        log.warn("Ошибка при создании пользователя. Пользователь с таким email уже существует");
        throw new RequestError(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
    }

    @Override
    public User updateUser(User user) {
        if(!checkNotDuplicateEmail(user)) {
            log.warn("Ошибка при обновлении email пользователя. Пользователь с таким email уже существует");
            throw new RequestError(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }
        if (checkContainsIdInUserMap(user.getId())) {
            log.info("Обновлена информация о пользователе с id = {}", user.getId());
            userMap.put(user.getId(), user);
            return user;
        }
        log.warn("Ошибка при обновлении пользователя. Пользователь с таким id не найден");
        throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
    }

    @Override
    public User updateEmailForUser(User user) {
        if (!checkContainsIdInUserMap(user.getId())) {
            log.warn("Ошибка при обновлении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
        }
        if (checkNotDuplicateEmail(user)) {
            userMap.get(user.getId()).setEmail(user.getEmail());
            log.info("Пользователь с id = {} обновил email на {}", user.getId(), user.getEmail());
            return userMap.get(user.getId());
        }
        log.warn("Ошибка при обновлении email пользователя. Пользователь с таким email уже существует");
        throw new RequestError(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
    }

    @Override
    public User updateNameForUser(User user) {
        if (!checkContainsIdInUserMap(user.getId())) {
            log.warn("Ошибка при обновлении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
        }
        log.info("Обновлено имя для пользователя с id = {}", user.getId());
        userMap.get(user.getId()).setName(user.getName());
        return userMap.get(user.getId());
    }

    @Override
    public User getUserById(Integer userId) {
        if (checkContainsIdInUserMap(userId)) {
            log.info("Запрошен пользователь с id = {}", userId);
            return userMap.get(userId);
        }
        log.warn("Ошибка при получении пользователя. Пользователь с таким id не найден");
        throw new RequestError(HttpStatus.NOT_FOUND, "Пользователь с таким id не найден");
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Запрошен список пользователей");
        return new ArrayList<>(userMap.values());
    }

    @Override
    public void deleteUserById(Integer userId) {
        if (!checkContainsIdInUserMap(userId)) {
            log.warn("Ошибка при удалении пользователя. Пользователь с таким id не найден");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Пользователь с таким id не найден");
        }
        userMap.remove(userId);
    }

    @Override
    public void deleteAllUser() {
        userMap.clear();
    }

    private boolean checkContainsIdInUserMap(Integer id) {
        return userMap.containsKey(id);
    }

    private boolean checkNotDuplicateEmail(User user) {
        return userMap.values().stream().noneMatch(mapUser -> mapUser.getEmail()
                .equals(user.getEmail()));
    }
}
