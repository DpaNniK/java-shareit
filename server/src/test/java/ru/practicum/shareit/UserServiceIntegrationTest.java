package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.practicum.shareit.exception.RequestError;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
        userService.deleteAllUser();
    }

    @Test
    public void testCreateUser() {
        User userTest = userService.createUser(createUserDto("иван"));

        Optional<User> userOptional = Optional.ofNullable(userService.getUserById(userTest.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", userTest.getId())
                                .hasFieldOrPropertyWithValue("name", userTest.getName())
                                .hasFieldOrPropertyWithValue("email", userTest.getEmail())
                );
    }

    @Test
    public void testUpdateUser() {
        User userTest = userService.createUser(createUserDto("иван"));
        UserDto userDto = (createUserDto("олег"));
        userDto.setId(userTest.getId());
        Optional<User> userOptional = Optional.ofNullable(userService.updateUser(userDto));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", userTest.getId())
                                .hasFieldOrPropertyWithValue("name", userDto.getName())
                                .hasFieldOrPropertyWithValue("email", userDto.getEmail())
                );
    }

    @Test
    public void testDeleteUserById() {
        User userTest = userService.createUser(createUserDto("иван"));
        userService.deleteUserById(userTest.getId());

        assertEquals(userService.getAllUsers().size(), 0, "Пользователь не удален");
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

    private Executable generateExecutableForUpdateNotFoundUser() {
        UserDto userDto = new UserDto();
        return () -> userService.updateUser(userDto);
    }

    private UserDto createUserDto(String name) {
        UserDto userDto = new UserDto();
        userDto.setId(1);
        userDto.setName(name);
        userDto.setEmail("yand@yandex.ru");
        return userDto;
    }
}
