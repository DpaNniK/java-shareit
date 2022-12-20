package ru.practicum.shareit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @AfterEach
    void updateRepository() {
        userService.deleteAllUser();
    }

    @Test
    public void testCreateUser() {
        User user = userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        assertEquals(user.getId(), 1 , "Неверно присвоен ID созданному пользователю");
        assertEquals(user.getName(), "Игорь" , "Неверно присвоено имя созданному пользователю");
        assertEquals(user.getEmail(), "mail@mail.ru" , "Неверно присвоен email созданному пользователю");
    }

    @Test
    public void testUpdateUser() {
        userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        UserDto userDto = new UserDto("Илья", "ilia@yandex.ru");
        userDto.setId(1);
        User updateUser = userService.updateUser(userDto);
        assertEquals(updateUser.getId(), 1 , "Неверный ID у обновленного пользователя");
        assertEquals(updateUser.getName(), "Илья" , "Неверно обновлено имя пользователя");
        assertEquals(updateUser.getEmail(), "ilia@yandex.ru" , "Неверно обновлен email пользователя");
    }

    @Test
    public void testDeleteUserById() {
        userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        userService.deleteUserById(1);
        assertEquals(userService.getAllUsers().size(), 0 , "Пользователь не удален");
    }

    @Test
    public void getRequestErrorForNotFoundUser() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForDeleteNotFoundUser()
        );
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    @Test
    public void getRequestErrorForDuplicateEmail() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForCreateDuplicateEmail()
        );
        assertEquals(HttpStatus.CONFLICT, er.getStatus());
    }

    @Test
    public void getRequestErrorForUpdateNotFoundUser() {
        RequestError er = Assertions.assertThrows(
                RequestError.class,
                generateExecutableForUpdateNotFoundUser()
        );
        assertEquals(HttpStatus.BAD_REQUEST, er.getStatus());
    }

    private Executable generateExecutableForDeleteNotFoundUser() {
        return () -> userService.deleteUserById(1);
    }

    private Executable generateExecutableForCreateDuplicateEmail() {
        userService.createUser(new UserDto("Игорь", "mail@mail.ru"));
        UserDto userDto = new UserDto("Илья", "mail@mail.ru");
        userDto.setId(1);
        return () -> userService.createUser(userDto);
    }

    private Executable generateExecutableForUpdateNotFoundUser() {
        UserDto userDto = new UserDto("Иван", "mail@mail.ru");
        return () -> userService.updateUser(userDto);
    }
}
